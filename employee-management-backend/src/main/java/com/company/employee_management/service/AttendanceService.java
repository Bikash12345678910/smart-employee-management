package com.company.employee_management.service;

import com.company.employee_management.dto.*;
import com.company.employee_management.entity.*;
import com.company.employee_management.exception.BadRequestException;
import com.company.employee_management.exception.ResourceNotFoundException;
import com.company.employee_management.integration.biometric.BiometricDeviceGateway;
import com.company.employee_management.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceEventRepository eventRepository;
    private final EmployeeRepository employeeRepository;
    private final HolidayRepository holidayRepository;
    private final CompanySettingRepository companySettingRepository;
    private final BiometricRegistrationRepository biometricRegistrationRepository;
    private final BiometricDeviceRepository biometricDeviceRepository;
    private final BiometricDeviceGateway biometricDeviceGateway;
    private final AuditLogService auditLogService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             AttendanceEventRepository eventRepository,
                             EmployeeRepository employeeRepository,
                             HolidayRepository holidayRepository,
                             CompanySettingRepository companySettingRepository,
                             BiometricRegistrationRepository biometricRegistrationRepository,
                             BiometricDeviceRepository biometricDeviceRepository,
                             BiometricDeviceGateway biometricDeviceGateway,
                             AuditLogService auditLogService) {
        this.attendanceRepository = attendanceRepository;
        this.eventRepository = eventRepository;
        this.employeeRepository = employeeRepository;
        this.holidayRepository = holidayRepository;
        this.companySettingRepository = companySettingRepository;
        this.biometricRegistrationRepository = biometricRegistrationRepository;
        this.biometricDeviceRepository = biometricDeviceRepository;
        this.biometricDeviceGateway = biometricDeviceGateway;
        this.auditLogService = auditLogService;
    }

    private CompanySetting getCompanySetting() {
        return companySettingRepository.findById(1L).orElseGet(() -> {
            CompanySetting s = new CompanySetting();
            return companySettingRepository.save(s);
        });
    }

    public Page<AttendanceDTO> getAttendanceWithFilters(LocalDate date, Long departmentId, String status, String search, Pageable pageable) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        String cleanStatus = (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) ? status.trim() : null;
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<Attendance> page = attendanceRepository.findWithFilters(queryDate, departmentId, cleanStatus, cleanSearch, pageable);
        return page.map(this::mapToDTO);
    }

    public List<AttendanceDTO> getEmployeeAttendanceHistory(Long employeeId) {
        return attendanceRepository.findByEmployeeIdOrderByAttendanceDateDesc(employeeId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Optional<AttendanceDTO> getTodayEmployeeAttendance(Long employeeId) {
        return attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, LocalDate.now())
                .map(this::mapToDTO);
    }

    @Transactional
    public AttendanceDTO recordAttendancePunch(AttendancePunchRequest req) {
        Employee employee = null;
        if (req.getEmployeeId() != null) {
            employee = employeeRepository.findById(req.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + req.getEmployeeId()));
        } else if (req.getEmployeeCode() != null) {
            employee = employeeRepository.findByEmployeeCode(req.getEmployeeCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + req.getEmployeeCode()));
        } else {
            throw new BadRequestException("Employee ID or Code is required");
        }

        CompanySetting settings = getCompanySetting();
        String method = req.getPunchMethod().toUpperCase();

        // Check if punch method is permitted by company settings
        if ("FACE".equals(method) && !Boolean.TRUE.equals(settings.getEnableFaceAttendance())) {
            throw new BadRequestException("Face attendance is disabled in company settings");
        }
        if ("FINGERPRINT".equals(method) && !Boolean.TRUE.equals(settings.getEnableFingerprintAttendance())) {
            throw new BadRequestException("Fingerprint attendance is disabled in company settings");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime punchDateTime = LocalDateTime.now().withNano(0);
        LocalTime now = punchDateTime.toLocalTime();
        String punchType = req.getPunchType().toUpperCase();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        Optional<AttendanceEvent> lastPunchOpt = eventRepository.findTopByEmployeeIdAndEventTimeBetweenOrderByEventTimeDesc(employee.getId(), startOfDay, endOfDay);

        if ("CHECK_IN".equals(punchType)) {
            if (lastPunchOpt.isPresent() && "CHECK_IN".equalsIgnoreCase(lastPunchOpt.get().getEventType())) {
                throw new BadRequestException("Employee is already checked in (last check-in at " + lastPunchOpt.get().getEventTime().toLocalTime() + "). Consecutive check-ins are not allowed.");
            }
        } else if ("CHECK_OUT".equals(punchType)) {
            if (lastPunchOpt.isEmpty() || !"CHECK_IN".equalsIgnoreCase(lastPunchOpt.get().getEventType())) {
                throw new BadRequestException("Employee cannot check out without an active check-in. Consecutive check-outs are not allowed.");
            }
        } else {
            throw new BadRequestException("Punch type must be CHECK_IN or CHECK_OUT");
        }

        // 1. Record raw event punch log in attendance_events
        AttendanceEvent newEvent = new AttendanceEvent();
        newEvent.setEmployee(employee);
        newEvent.setEventType(punchType);
        newEvent.setPunchMethod(method);
        newEvent.setEventTime(punchDateTime);
        if (req.getDeviceId() != null) {
            biometricDeviceRepository.findById(req.getDeviceId()).ifPresent(newEvent::setDevice);
        }
        eventRepository.save(newEvent);

        // 2. Fetch all punch events today in chronological order to compute exact working minutes
        List<AttendanceEvent> todayEvents = eventRepository.findByEmployeeIdAndEventTimeBetweenOrderByEventTimeAsc(employee.getId(), startOfDay, endOfDay);

        int totalWorkingMinutes = 0;
        LocalDateTime firstCheckIn = null;
        LocalDateTime lastCheckOut = null;
        LocalDateTime currentIn = null;

        for (AttendanceEvent ev : todayEvents) {
            if ("CHECK_IN".equalsIgnoreCase(ev.getEventType())) {
                if (firstCheckIn == null) {
                    firstCheckIn = ev.getEventTime();
                }
                currentIn = ev.getEventTime();
            } else if ("CHECK_OUT".equalsIgnoreCase(ev.getEventType()) && currentIn != null) {
                long diff = ChronoUnit.MINUTES.between(currentIn, ev.getEventTime());
                if (diff > 0) {
                    totalWorkingMinutes += (int) diff;
                }
                lastCheckOut = ev.getEventTime();
                currentIn = null;
            }
        }

        Optional<Attendance> existingOpt = attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), today);
        Attendance attendance = existingOpt.orElse(new Attendance());
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(today);
        if (firstCheckIn != null) {
            attendance.setCheckInTime(firstCheckIn.toLocalTime());
        }
        if (lastCheckOut != null) {
            attendance.setCheckOutTime(lastCheckOut.toLocalTime());
        }
        attendance.setAttendanceVia(method);
        attendance.setWorkingHours(totalWorkingMinutes / 60);
        attendance.setWorkingMinutes(totalWorkingMinutes % 60);

        // Shift late arrival calculation based on first check-in
        LocalTime shiftStart = settings.getShiftStartTime();
        int graceMinutes = settings.getLateArrivalGraceMinutes();
        LocalTime graceTime = shiftStart.plusMinutes(graceMinutes);

        if (firstCheckIn != null && firstCheckIn.toLocalTime().isAfter(graceTime)) {
            long lateMins = ChronoUnit.MINUTES.between(shiftStart, firstCheckIn.toLocalTime());
            attendance.setLateMinutes((int) lateMins);
            attendance.setAttendanceStatus("LATE");
        } else {
            attendance.setLateMinutes(0);
            attendance.setAttendanceStatus("PRESENT");
        }

        // Half-day check if total working minutes completed is below threshold
        int halfDayMins = settings.getHalfDayThresholdHours() * 60;
        if (totalWorkingMinutes > 0 && totalWorkingMinutes < halfDayMins && "CHECK_OUT".equals(punchType)) {
            attendance.setAttendanceStatus("HALF_DAY");
        }

        attendance = attendanceRepository.save(attendance);
        return mapToDTO(attendance);
    }

    @Transactional
    public AttendanceDTO applyManualCorrection(ManualAttendanceCorrectionRequest req, String adminUsername) {
        Attendance attendance;
        if (req.getAttendanceId() != null) {
            attendance = attendanceRepository.findById(req.getAttendanceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));
        } else {
            Employee emp = employeeRepository.findById(req.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
            attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(emp.getId(), req.getAttendanceDate())
                    .orElseGet(() -> {
                        Attendance a = new Attendance();
                        a.setEmployee(emp);
                        a.setAttendanceDate(req.getAttendanceDate());
                        return a;
                    });
        }

        if (req.getCheckInTime() != null) attendance.setCheckInTime(req.getCheckInTime());
        if (req.getCheckOutTime() != null) attendance.setCheckOutTime(req.getCheckOutTime());
        if (req.getAttendanceStatus() != null) attendance.setAttendanceStatus(req.getAttendanceStatus());

        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            long totalMinutes = ChronoUnit.MINUTES.between(attendance.getCheckInTime(), attendance.getCheckOutTime());
            int netWorkingMinutes = Math.max(0, (int) totalMinutes - attendance.getTotalBreakMinutes());
            attendance.setWorkingHours(netWorkingMinutes / 60);
            attendance.setWorkingMinutes(netWorkingMinutes % 60);
        }

        attendance.setIsManualCorrection(true);
        attendance.setCorrectionReason(req.getCorrectionReason());
        attendance.setAttendanceVia("MANUAL");

        attendance = attendanceRepository.save(attendance);

        auditLogService.logAction(adminUsername, "ATTENDANCE_CORRECTION", "ATTENDANCE", attendance.getId().toString(),
                "Manual attendance override: " + req.getCorrectionReason());

        return mapToDTO(attendance);
    }

    private boolean isWeeklyOffDay(LocalDate date, CompanySetting settings) {
        String off = (settings != null && settings.getWeeklyOffDays() != null)
                ? settings.getWeeklyOffDays().trim().toUpperCase() : "SUNDAY";
        DayOfWeek dow = date.getDayOfWeek();
        if ("SATURDAY_SUNDAY".equals(off)) {
            return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
        }
        if ("FRIDAY".equals(off)) {
            return dow == DayOfWeek.FRIDAY;
        }
        return dow == DayOfWeek.SUNDAY;
    }

    @Transactional
    public void markAbsentForUnmarkedEmployees(LocalDate date) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<Employee> allEmployees = employeeRepository.findAll();
        CompanySetting settings = companySettingRepository.findAll().stream().findFirst().orElse(null);
        boolean isHoliday = holidayRepository.existsByHolidayDate(queryDate);
        boolean isWeeklyOff = isWeeklyOffDay(queryDate, settings);

        for (Employee emp : allEmployees) {
            Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndAttendanceDate(emp.getId(), queryDate);
            if (existing.isEmpty()) {
                Attendance att = new Attendance();
                att.setEmployee(emp);
                att.setAttendanceDate(queryDate);
                if (isHoliday || isWeeklyOff) {
                    att.setAttendanceStatus("HOLIDAY");
                } else {
                    att.setAttendanceStatus("ABSENT");
                }
                att.setAttendanceVia("SYSTEM");
                attendanceRepository.save(att);
            }
        }
    }

    public Map<String, Object> getMonthlyAttendanceMatrix(Integer month, Integer year) {
        int qMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int qYear = (year != null) ? year : LocalDate.now().getYear();

        YearMonth ym = YearMonth.of(qYear, qMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int daysInMonth = ym.lengthOfMonth();

        List<Employee> employees = employeeRepository.findAll();
        List<Attendance> attendances = attendanceRepository.findByAttendanceDateBetweenOrderByAttendanceDateAsc(start, end);
        List<Holiday> holidays = holidayRepository.findByYearOrderByHolidayDateAsc(qYear);
        Set<LocalDate> holidayDates = holidays.stream().map(Holiday::getHolidayDate).collect(Collectors.toSet());

        // Group attendances by employeeId -> date -> attendance
        Map<Long, Map<LocalDate, Attendance>> attMap = new HashMap<>();
        for (Attendance a : attendances) {
            attMap.computeIfAbsent(a.getEmployee().getId(), k -> new HashMap<>())
                    .put(a.getAttendanceDate(), a);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Employee emp : employees) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("employeeId", emp.getId());
            row.put("employeeCode", emp.getEmployeeCode());
            row.put("employeeName", emp.getFullName());
            row.put("department", emp.getDepartment() != null ? emp.getDepartment().getName() : "");

            int totalHours = 0;
            int totalMins = 0;
            int presentCount = 0;
            int absentCount = 0;
            int lateCount = 0;
            int halfDayCount = 0;
            int holidayCount = 0;
            int leaveCount = 0;

            Map<String, String> dailyStatus = new LinkedHashMap<>();
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate d = ym.atDay(day);
                Attendance a = attMap.getOrDefault(emp.getId(), Collections.emptyMap()).get(d);
                String code = "-";
                if (a != null) {
                    totalHours += (a.getWorkingHours() != null ? a.getWorkingHours() : 0);
                    totalMins += (a.getWorkingMinutes() != null ? a.getWorkingMinutes() : 0);
                    switch (a.getAttendanceStatus()) {
                        case "PRESENT" -> { code = "P"; presentCount++; }
                        case "LATE" -> { code = "L"; lateCount++; presentCount++; }
                        case "HALF_DAY" -> { code = "H"; halfDayCount++; presentCount++; }
                        case "ABSENT" -> { code = "A"; absentCount++; }
                        case "ON_LEAVE" -> { code = "LV"; leaveCount++; }
                        case "HOLIDAY" -> { code = "HL"; holidayCount++; }
                        default -> code = "P";
                    }
                } else {
                    CompanySetting matrixSettings = companySettingRepository.findAll().stream().findFirst().orElse(null);
                    if (holidayDates.contains(d) || isWeeklyOffDay(d, matrixSettings)) {
                        code = "HL";
                        holidayCount++;
                    } else if (d.isBefore(LocalDate.now())) {
                        code = "A";
                        absentCount++;
                    }
                }
                dailyStatus.put(String.valueOf(day), code);
            }

            int calculatedTotalMinutes = (totalHours * 60) + totalMins;
            totalHours += (totalMins / 60);
            totalMins = totalMins % 60;

            row.put("daily", dailyStatus);
            row.put("totalHours", totalHours);
            row.put("totalMinutes", totalMins);
            row.put("totalWorkingMinutes", calculatedTotalMinutes);
            row.put("presentDays", presentCount);
            row.put("absentDays", absentCount);
            row.put("lateDays", lateCount);
            row.put("halfDays", halfDayCount);
            row.put("leaveDays", leaveCount);
            row.put("holidayDays", holidayCount);

            int workingDays = Math.max(1, daysInMonth - holidayCount);
            double pct = Math.min(100.0, Math.round(((double) presentCount / workingDays) * 1000.0) / 10.0);
            row.put("attendancePercentage", pct);

            rows.add(row);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("month", qMonth);
        result.put("year", qYear);
        result.put("daysInMonth", daysInMonth);
        result.put("records", rows);
        return result;
    }

    public String exportMonthlyAttendanceCsv(Integer month, Integer year) {
        Map<String, Object> data = getMonthlyAttendanceMatrix(month, year);
        int daysInMonth = (int) data.get("daysInMonth");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // Header
        StringBuilder sb = new StringBuilder("Employee ID,Employee Name,Department");
        for (int i = 1; i <= daysInMonth; i++) {
            sb.append(",").append(i);
        }
        sb.append(",Total Working Minutes,Total Hours,Total Mins,Present Days,Absent Days,Attendance %");
        pw.println(sb.toString());

        for (Map<String, Object> r : records) {
            StringBuilder rowSb = new StringBuilder();
            rowSb.append("\"").append(r.get("employeeCode")).append("\",");
            rowSb.append("\"").append(r.get("employeeName")).append("\",");
            rowSb.append("\"").append(r.get("department")).append("\"");

            @SuppressWarnings("unchecked")
            Map<String, String> daily = (Map<String, String>) r.get("daily");
            for (int i = 1; i <= daysInMonth; i++) {
                rowSb.append(",").append(daily.getOrDefault(String.valueOf(i), "-"));
            }

            rowSb.append(",").append(r.get("totalWorkingMinutes")).append(" mins");
            rowSb.append(",").append(r.get("totalHours"));
            rowSb.append(",").append(r.get("totalMinutes"));
            rowSb.append(",").append(r.get("presentDays"));
            rowSb.append(",").append(r.get("absentDays"));
            rowSb.append(",").append(r.get("attendancePercentage")).append("%");
            pw.println(rowSb.toString());
        }

        return sw.toString();
    }

    public DailyPunchSummaryDTO getDailyPunchSummary(Long employeeId, LocalDate date) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        LocalDateTime startOfDay = queryDate.atStartOfDay();
        LocalDateTime endOfDay = queryDate.atTime(LocalTime.MAX);
        List<AttendanceEvent> events = eventRepository.findByEmployeeIdAndEventTimeBetweenOrderByEventTimeAsc(employeeId, startOfDay, endOfDay);

        DailyPunchSummaryDTO summary = new DailyPunchSummaryDTO();
        summary.setEmployeeId(emp.getId());
        summary.setEmployeeName(emp.getFullName());
        summary.setEmployeeCode(emp.getEmployeeCode());
        summary.setDate(queryDate);

        List<DailyPunchPairDTO> pairs = new ArrayList<>();
        List<AttendanceEventDTO> eventDTOs = new ArrayList<>();

        int seq = 1;
        int totalMins = 0;
        AttendanceEvent activeIn = null;

        for (AttendanceEvent ev : events) {
            String devName = ev.getDevice() != null ? ev.getDevice().getDeviceName() : "Mobile App";
            eventDTOs.add(new AttendanceEventDTO(ev.getId(), emp.getId(), emp.getEmployeeCode(), emp.getFullName(),
                    ev.getEventType(), ev.getEventTime(), ev.getPunchMethod(),
                    ev.getDevice() != null ? ev.getDevice().getId() : null, devName));

            if ("CHECK_IN".equalsIgnoreCase(ev.getEventType())) {
                activeIn = ev;
            } else if ("CHECK_OUT".equalsIgnoreCase(ev.getEventType())) {
                if (activeIn != null) {
                    long mins = ChronoUnit.MINUTES.between(activeIn.getEventTime(), ev.getEventTime());
                    int pairMins = Math.max(0, (int) mins);
                    totalMins += pairMins;
                    pairs.add(new DailyPunchPairDTO(seq++, activeIn.getEventTime().toLocalTime(), ev.getEventTime().toLocalTime(),
                            pairMins, activeIn.getPunchMethod(), ev.getPunchMethod(), "COMPLETED"));
                    activeIn = null;
                }
            }
        }

        if (activeIn != null) {
            long mins = ChronoUnit.MINUTES.between(activeIn.getEventTime(), LocalDateTime.now());
            int inProgressMins = Math.max(0, (int) mins);
            pairs.add(new DailyPunchPairDTO(seq++, activeIn.getEventTime().toLocalTime(), null,
                    inProgressMins, activeIn.getPunchMethod(), null, "IN_PROGRESS"));
            summary.setCurrentStatus("CHECKED_IN");
        } else if (!events.isEmpty()) {
            summary.setCurrentStatus("CHECKED_OUT");
        } else {
            summary.setCurrentStatus("NOT_REPORTED");
        }

        summary.setPunchPairs(pairs);
        summary.setRawEvents(eventDTOs);
        summary.setTotalWorkingMinutes(totalMins);
        return summary;
    }

    public String exportAttendanceCsv(LocalDate date, Long departmentId, String status, String search) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<AttendanceDTO> list = attendanceRepository.findWithFilters(queryDate, departmentId,
                (status != null && !status.equalsIgnoreCase("ALL")) ? status : null,
                search, org.springframework.data.domain.Pageable.unpaged())
                .map(this::mapToDTO).getContent();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Employee ID,Employee Name,Department,Date,Check-In,Check-Out,Working Minutes,Status,Attendance Via");

        for (AttendanceDTO a : list) {
            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%d mins\",\"%s\",\"%s\"%n",
                    a.getEmployeeCode() != null ? a.getEmployeeCode() : "-",
                    a.getEmployeeName() != null ? a.getEmployeeName() : "-",
                    a.getDepartmentName() != null ? a.getDepartmentName() : "-",
                    a.getAttendanceDate() != null ? a.getAttendanceDate() : queryDate,
                    a.getCheckInTime() != null ? a.getCheckInTime() : "-",
                    a.getCheckOutTime() != null ? a.getCheckOutTime() : "-",
                    a.getTotalWorkingMinutes(),
                    a.getAttendanceStatus(),
                    a.getAttendanceVia() != null ? a.getAttendanceVia() : "MANUAL"
            );
        }
        return sw.toString();
    }

    public String exportEmployeeAttendanceCsv(Long employeeId, Integer month, Integer year) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        List<Attendance> list;
        if (month != null && year != null) {
            YearMonth ym = YearMonth.of(year, month);
            list = attendanceRepository.findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                    employeeId, ym.atDay(1), ym.atEndOfMonth());
        } else {
            list = attendanceRepository.findByEmployeeIdOrderByAttendanceDateDesc(employeeId);
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.printf("# Attendance Records for %s (%s)%n", emp.getFullName(), emp.getEmployeeCode());
        pw.println("Date,Check-In,Check-Out,Working Minutes,Status,Method");

        for (Attendance a : list) {
            int mins = (a.getWorkingHours() != null ? a.getWorkingHours() : 0) * 60 +
                       (a.getWorkingMinutes() != null ? a.getWorkingMinutes() : 0);
            pw.printf("\"%s\",\"%s\",\"%s\",\"%d mins\",\"%s\",\"%s\"%n",
                    a.getAttendanceDate(),
                    a.getCheckInTime() != null ? a.getCheckInTime() : "-",
                    a.getCheckOutTime() != null ? a.getCheckOutTime() : "-",
                    mins,
                    a.getAttendanceStatus(),
                    a.getAttendanceVia() != null ? a.getAttendanceVia() : "BIOMETRIC"
            );
        }
        return sw.toString();
    }

    public AttendanceDTO mapToDTO(Attendance att) {
        if (att == null) return null;
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(att.getId());
        if (att.getEmployee() != null) {
            dto.setEmployeeId(att.getEmployee().getId());
            dto.setEmployeeCode(att.getEmployee().getEmployeeCode());
            dto.setEmployeeName(att.getEmployee().getFullName());
            dto.setProfilePhotoUrl(att.getEmployee().getProfilePhotoUrl());
            if (att.getEmployee().getDepartment() != null) {
                dto.setDepartmentName(att.getEmployee().getDepartment().getName());
            }
            if (att.getEmployee().getDesignation() != null) {
                dto.setDesignationTitle(att.getEmployee().getDesignation().getTitle());
            }
        }
        dto.setAttendanceDate(att.getAttendanceDate());
        dto.setCheckInTime(att.getCheckInTime());
        dto.setCheckOutTime(att.getCheckOutTime());
        dto.setWorkingHours(att.getWorkingHours());
        dto.setWorkingMinutes(att.getWorkingMinutes());
        dto.setAttendanceStatus(att.getAttendanceStatus());
        dto.setAttendanceVia(att.getAttendanceVia());
        dto.setBreakCount(att.getBreakCount());
        dto.setTotalBreakMinutes(att.getTotalBreakMinutes());
        dto.setLateMinutes(att.getLateMinutes());
        dto.setEarlyDepartureMinutes(att.getEarlyDepartureMinutes());
        dto.setIsManualCorrection(att.getIsManualCorrection());
        dto.setCorrectionReason(att.getCorrectionReason());
        return dto;
    }
}
