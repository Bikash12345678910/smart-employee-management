package com.company.employee_management.service;

import com.company.employee_management.dto.*;
import com.company.employee_management.entity.*;
import com.company.employee_management.exception.ResourceNotFoundException;
import com.company.employee_management.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final DepartmentRepository departmentRepository;
    private final BiometricDeviceRepository deviceRepository;
    private final PayslipRepository payslipRepository;
    private final HolidayRepository holidayRepository;
    private final EmployeeBreakRepository breakRepository;
    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final HolidayService holidayService;

    public DashboardService(EmployeeRepository employeeRepository,
                            AttendanceRepository attendanceRepository,
                            DepartmentRepository departmentRepository,
                            BiometricDeviceRepository deviceRepository,
                            PayslipRepository payslipRepository,
                            HolidayRepository holidayRepository,
                            EmployeeBreakRepository breakRepository,
                            EmployeeService employeeService,
                            AttendanceService attendanceService,
                            HolidayService holidayService) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.departmentRepository = departmentRepository;
        this.deviceRepository = deviceRepository;
        this.payslipRepository = payslipRepository;
        this.holidayRepository = holidayRepository;
        this.breakRepository = breakRepository;
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
        this.holidayService = holidayService;
    }

    public AdminDashboardStatsDTO getAdminDashboardStats() {
        AdminDashboardStatsDTO dto = new AdminDashboardStatsDTO();

        long totalEmp = employeeRepository.countByIsActive(true);
        dto.setTotalEmployees(totalEmp);

        LocalDate today = LocalDate.now();
        List<Attendance> todayAttendances = attendanceRepository.findByAttendanceDate(today);

        long present = 0;
        long late = 0;
        long halfDay = 0;
        long onLeave = 0;
        long absent = 0;

        for (Attendance a : todayAttendances) {
            switch (a.getAttendanceStatus()) {
                case "PRESENT" -> present++;
                case "LATE" -> { late++; present++; }
                case "HALF_DAY" -> { halfDay++; present++; }
                case "ON_LEAVE" -> onLeave++;
                case "ABSENT" -> absent++;
            }
        }

        // Any employee without attendance record for today is considered absent or unmarked
        long markedCount = todayAttendances.size();
        if (markedCount < totalEmp) {
            absent += (totalEmp - markedCount);
        }

        dto.setPresentToday(present);
        dto.setLateToday(late);
        dto.setHalfDayToday(halfDay);
        dto.setOnLeaveToday(onLeave);
        dto.setAbsentToday(absent);

        double pct = (totalEmp > 0) ? Math.round(((double) present / totalEmp) * 1000.0) / 10.0 : 0.0;
        dto.setAttendancePercentage(pct);

        // Salary payout for current month
        int curMonth = today.getMonthValue();
        int curYear = today.getYear();
        BigDecimal totalPayout = payslipRepository.getTotalSalaryPayoutForMonth(curMonth, curYear);
        if (totalPayout == null) {
            // Estimate based on agreed salary of active employees
            List<Employee> all = employeeRepository.findAll();
            totalPayout = all.stream()
                    .map(Employee::getAgreedMonthlySalary)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        dto.setMonthlySalaryPayout(totalPayout);

        dto.setTotalDepartments((int) departmentRepository.count());
        dto.setOnlineDevices((int) deviceRepository.count());

        // Trend for last 7 days
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            List<Attendance> dayAtts = attendanceRepository.findByAttendanceDate(d);
            long p = dayAtts.stream().filter(a -> "PRESENT".equals(a.getAttendanceStatus()) || "LATE".equals(a.getAttendanceStatus()) || "HALF_DAY".equals(a.getAttendanceStatus())).count();
            long l = dayAtts.stream().filter(a -> "LATE".equals(a.getAttendanceStatus())).count();
            long ab = dayAtts.stream().filter(a -> "ABSENT".equals(a.getAttendanceStatus())).count();
            if (dayAtts.size() < totalEmp && !d.equals(today)) {
                ab += (totalEmp - dayAtts.size());
            }

            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", d.toString());
            dayMap.put("day", d.getDayOfWeek().toString().substring(0, 3));
            dayMap.put("present", p);
            dayMap.put("late", l);
            dayMap.put("absent", ab);
            trend.add(dayMap);
        }
        dto.setMonthlyAttendanceTrend(trend);

        // Department distribution
        List<Map<String, Object>> deptDist = new ArrayList<>();
        List<Department> depts = departmentRepository.findAll();
        List<Employee> allEmps = employeeRepository.findAll();
        for (Department dept : depts) {
            long count = allEmps.stream().filter(e -> e.getDepartment() != null && e.getDepartment().getId().equals(dept.getId())).count();
            Map<String, Object> m = new HashMap<>();
            m.put("department", dept.getName());
            m.put("code", dept.getDepartmentCode());
            m.put("count", count);
            deptDist.add(m);
        }
        dto.setDepartmentDistribution(deptDist);

        // Recent activity
        List<AttendanceDTO> recentActivity = todayAttendances.stream()
                .filter(a -> a.getCheckInTime() != null)
                .sorted((a, b) -> b.getCheckInTime().compareTo(a.getCheckInTime()))
                .limit(6)
                .map(attendanceService::mapToDTO)
                .collect(Collectors.toList());
        dto.setRecentAttendanceActivity(recentActivity);

        // Recent registrations
        List<EmployeeDTO> recentEmps = employeeRepository.findAll(PageRequest.of(0, 5)).getContent()
                .stream().map(employeeService::mapToDTO).collect(Collectors.toList());
        dto.setRecentRegistrations(recentEmps);

        return dto;
    }

    public EmployeeDashboardStatsDTO getEmployeeDashboardStats(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        EmployeeDashboardStatsDTO dto = new EmployeeDashboardStatsDTO();
        dto.setEmployeeId(emp.getId());
        dto.setEmployeeCode(emp.getEmployeeCode());
        dto.setFullName(emp.getFullName());
        if (emp.getDepartment() != null) dto.setDepartmentName(emp.getDepartment().getName());
        if (emp.getDesignation() != null) dto.setDesignationTitle(emp.getDesignation().getTitle());
        dto.setProfilePhotoUrl(emp.getProfilePhotoUrl());

        LocalDate today = LocalDate.now();
        dto.setTodayDate(today);

        Optional<Attendance> todayAtt = attendanceRepository.findByEmployeeIdAndAttendanceDate(emp.getId(), today);
        if (todayAtt.isPresent()) {
            Attendance a = todayAtt.get();
            dto.setTodayStatus(a.getAttendanceStatus());
            dto.setCheckInTime(a.getCheckInTime());
            dto.setCheckOutTime(a.getCheckOutTime());
            dto.setTodayWorkingHours(a.getWorkingHours());
            dto.setTodayWorkingMinutes(a.getWorkingMinutes());
        } else {
            dto.setTodayStatus("NOT_MARKED");
        }

        // Check if on break today
        List<EmployeeBreak> todayBreaks = breakRepository.findByEmployeeIdAndBreakDate(emp.getId(), today);
        Optional<EmployeeBreak> activeBreak = todayBreaks.stream().filter(b -> b.getBreakEndTime() == null).findFirst();
        if (activeBreak.isPresent()) {
            dto.setIsOnBreak(true);
            dto.setCurrentBreakStart(activeBreak.get().getBreakStartTime());
        }

        // Current Month stats
        YearMonth ym = YearMonth.of(today.getYear(), today.getMonthValue());
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Attendance> monthAtts = attendanceRepository
                .findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(emp.getId(), start, end);

        long present = 0;
        long absent = 0;
        long leave = 0;
        double totalHours = 0.0;

        for (Attendance a : monthAtts) {
            String s = a.getAttendanceStatus();
            if ("PRESENT".equals(s) || "LATE".equals(s) || "HALF_DAY".equals(s)) {
                present++;
            } else if ("ON_LEAVE".equals(s)) {
                leave++;
            } else if ("ABSENT".equals(s)) {
                absent++;
            }
            if (a.getWorkingHours() != null) totalHours += a.getWorkingHours();
            if (a.getWorkingMinutes() != null) totalHours += (a.getWorkingMinutes() / 60.0);
        }

        dto.setPresentDays(present);
        dto.setAbsentDays(absent);
        dto.setLeaveDays(leave);
        dto.setTotalWorkingHoursMonth(Math.round(totalHours * 10.0) / 10.0);

        int totalDaysPassed = Math.min(today.getDayOfMonth(), ym.lengthOfMonth());
        double pct = (totalDaysPassed > 0) ? Math.min(100.0, Math.round(((double) present / totalDaysPassed) * 1000.0) / 10.0) : 0.0;
        dto.setAttendancePercentage(pct);

        // Recent punches
        dto.setRecentAttendance(attendanceService.getEmployeeAttendanceHistory(emp.getId()).stream().limit(5).collect(Collectors.toList()));

        // Upcoming holidays
        List<HolidayDTO> allHolidays = holidayService.getHolidays(today.getYear());
        List<HolidayDTO> upcoming = allHolidays.stream()
                .filter(h -> !h.getHolidayDate().isBefore(today))
                .limit(3)
                .collect(Collectors.toList());
        dto.setUpcomingHolidays(upcoming);

        return dto;
    }
}
