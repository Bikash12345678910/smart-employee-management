package com.company.employee_management.service;

import com.company.employee_management.dto.BreakActionRequest;
import com.company.employee_management.dto.BreakDTO;
import com.company.employee_management.entity.Attendance;
import com.company.employee_management.entity.CompanySetting;
import com.company.employee_management.entity.Employee;
import com.company.employee_management.entity.EmployeeBreak;
import com.company.employee_management.exception.BadRequestException;
import com.company.employee_management.exception.ResourceNotFoundException;
import com.company.employee_management.repository.AttendanceRepository;
import com.company.employee_management.repository.CompanySettingRepository;
import com.company.employee_management.repository.EmployeeBreakRepository;
import com.company.employee_management.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BreakService {

    private final EmployeeBreakRepository breakRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanySettingRepository companySettingRepository;
    private final AuditLogService auditLogService;

    public BreakService(EmployeeBreakRepository breakRepository,
                        AttendanceRepository attendanceRepository,
                        EmployeeRepository employeeRepository,
                        CompanySettingRepository companySettingRepository,
                        AuditLogService auditLogService) {
        this.breakRepository = breakRepository;
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.companySettingRepository = companySettingRepository;
        this.auditLogService = auditLogService;
    }

    private CompanySetting getCompanySetting() {
        return companySettingRepository.findById(1L).orElseGet(() -> {
            CompanySetting s = new CompanySetting();
            return companySettingRepository.save(s);
        });
    }

    @Transactional
    public BreakDTO handleBreakAction(BreakActionRequest req, String username) {
        Employee emp = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + req.getEmployeeId()));

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().withNano(0);

        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(emp.getId(), today)
                .orElseThrow(() -> new BadRequestException("Must check in today before taking breaks"));

        if ("START".equalsIgnoreCase(req.getAction())) {
            // Check if already on active break
            List<EmployeeBreak> todayBreaks = breakRepository.findByEmployeeIdAndBreakDate(emp.getId(), today);
            boolean onActiveBreak = todayBreaks.stream().anyMatch(b -> b.getBreakEndTime() == null);
            if (onActiveBreak) {
                throw new BadRequestException("You are already on an active break. Please end it first.");
            }

            CompanySetting setting = getCompanySetting();
            if (todayBreaks.size() >= setting.getMaxBreaksAllowed()) {
                throw new BadRequestException("Maximum breaks allowed today (" + setting.getMaxBreaksAllowed() + ") reached.");
            }

            EmployeeBreak brk = new EmployeeBreak();
            brk.setAttendance(attendance);
            brk.setEmployee(emp);
            brk.setBreakDate(today);
            brk.setBreakStartTime(now);
            brk.setBreakReason(req.getReason() != null ? req.getReason() : "TEA/LUNCH");
            brk = breakRepository.save(brk);

            return mapToDTO(brk);

        } else if ("END".equalsIgnoreCase(req.getAction())) {
            List<EmployeeBreak> todayBreaks = breakRepository.findByEmployeeIdAndBreakDate(emp.getId(), today);
            Optional<EmployeeBreak> activeOpt = todayBreaks.stream().filter(b -> b.getBreakEndTime() == null).findFirst();

            if (activeOpt.isEmpty()) {
                throw new BadRequestException("No active break to end.");
            }

            EmployeeBreak brk = activeOpt.get();
            brk.setBreakEndTime(now);
            long minutes = ChronoUnit.MINUTES.between(brk.getBreakStartTime(), now);
            brk.setDurationMinutes((int) minutes);
            brk = breakRepository.save(brk);

            // Update attendance totals
            int totalMins = (attendance.getTotalBreakMinutes() != null ? attendance.getTotalBreakMinutes() : 0) + (int) minutes;
            int count = (attendance.getBreakCount() != null ? attendance.getBreakCount() : 0) + 1;
            attendance.setTotalBreakMinutes(totalMins);
            attendance.setBreakCount(count);
            attendanceRepository.save(attendance);

            return mapToDTO(brk);
        } else {
            throw new BadRequestException("Action must be START or END");
        }
    }

    public Page<BreakDTO> getBreaks(LocalDate date, Long employeeId, Pageable pageable) {
        return breakRepository.findWithFilters(date, employeeId, pageable).map(this::mapToDTO);
    }

    public List<BreakDTO> getEmployeeBreaks(Long employeeId) {
        return breakRepository.findByEmployeeIdOrderByBreakDateDescBreakStartTimeDesc(employeeId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public String exportBreaksCsv(LocalDate date, Long employeeId) {
        List<EmployeeBreak> list;
        if (date != null) {
            list = breakRepository.findWithFilters(date, employeeId, Pageable.unpaged()).getContent();
        } else {
            list = breakRepository.findAll();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Employee ID,Employee Name,Date,Break Start,Break End,Duration (Mins),Reason");
        for (EmployeeBreak b : list) {
            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%d\",\"%s\"%n",
                    b.getEmployee() != null ? b.getEmployee().getEmployeeCode() : "",
                    b.getEmployee() != null ? b.getEmployee().getFullName() : "",
                    b.getBreakDate(),
                    b.getBreakStartTime(),
                    b.getBreakEndTime() != null ? b.getBreakEndTime() : "IN_PROGRESS",
                    b.getDurationMinutes(),
                    b.getBreakReason()
            );
        }
        return sw.toString();
    }

    public BreakDTO mapToDTO(EmployeeBreak brk) {
        if (brk == null) return null;
        BreakDTO dto = new BreakDTO();
        dto.setId(brk.getId());
        if (brk.getAttendance() != null) {
            dto.setAttendanceId(brk.getAttendance().getId());
        }
        if (brk.getEmployee() != null) {
            dto.setEmployeeId(brk.getEmployee().getId());
            dto.setEmployeeCode(brk.getEmployee().getEmployeeCode());
            dto.setEmployeeName(brk.getEmployee().getFullName());
        }
        dto.setBreakDate(brk.getBreakDate());
        dto.setBreakStartTime(brk.getBreakStartTime());
        dto.setBreakEndTime(brk.getBreakEndTime());
        dto.setDurationMinutes(brk.getDurationMinutes());
        dto.setBreakReason(brk.getBreakReason());
        return dto;
    }
}
