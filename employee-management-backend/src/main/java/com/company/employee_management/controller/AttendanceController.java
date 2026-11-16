package com.company.employee_management.controller;

import com.company.employee_management.dto.ApiResponse;
import com.company.employee_management.dto.AttendanceDTO;
import com.company.employee_management.dto.AttendancePunchRequest;
import com.company.employee_management.dto.ManualAttendanceCorrectionRequest;
import com.company.employee_management.entity.Employee;
import com.company.employee_management.exception.ResourceNotFoundException;
import com.company.employee_management.repository.EmployeeRepository;
import com.company.employee_management.security.UserPrincipal;
import com.company.employee_management.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;

    public AttendanceController(AttendanceService attendanceService, EmployeeRepository employeeRepository) {
        this.attendanceService = attendanceService;
        this.employeeRepository = employeeRepository;
    }

    // --- Admin Endpoints ---

    @GetMapping("/admin/attendance")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AttendanceDTO>>> getAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<AttendanceDTO> result = attendanceService.getAttendanceWithFilters(date, departmentId, status, search, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.ok("Attendance records retrieved", result));
    }

    @PostMapping("/admin/attendance/punch")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDTO>> adminPunchAttendance(
            @Valid @RequestBody AttendancePunchRequest request) {
        AttendanceDTO dto = attendanceService.recordAttendancePunch(request);
        return ResponseEntity.ok(ApiResponse.ok("Attendance punch recorded", dto));
    }

    @PostMapping("/admin/attendance/correction")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDTO>> correctAttendance(
            @Valid @RequestBody ManualAttendanceCorrectionRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        AttendanceDTO dto = attendanceService.applyManualCorrection(request, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Attendance corrected successfully", dto));
    }

    @PostMapping("/admin/attendance/mark-absent")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> markAbsent(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        attendanceService.markAbsentForUnmarkedEmployees(date);
        return ResponseEntity.ok(ApiResponse.ok("Absent status evaluated for unmarked employees", "Success"));
    }

    @GetMapping("/admin/attendance/events/{employeeId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<com.company.employee_management.dto.DailyPunchSummaryDTO>> getEmployeeDailyPunches(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var summary = attendanceService.getDailyPunchSummary(employeeId, date);
        return ResponseEntity.ok(ApiResponse.ok("Employee punch events retrieved", summary));
    }

    @GetMapping("/admin/attendance/export-csv")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportDailyAttendanceCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        String csv = attendanceService.exportAttendanceCsv(date, departmentId, status, search);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance-" + (date != null ? date : LocalDate.now()) + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    @GetMapping("/admin/attendance/export-employee-csv/{employeeId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportIndividualAttendanceCsv(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        String csv = attendanceService.exportEmployeeAttendanceCsv(employeeId, month, year);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employee-attendance-" + employeeId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    @GetMapping("/admin/attendance/monthly-matrix")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyMatrix(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        Map<String, Object> matrix = attendanceService.getMonthlyAttendanceMatrix(month, year);
        return ResponseEntity.ok(ApiResponse.ok("Monthly attendance records matrix retrieved", matrix));
    }

    @GetMapping("/admin/attendance/export-monthly-csv")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportMonthlyCsv(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        String csvData = attendanceService.exportMonthlyAttendanceCsv(month, year);
        byte[] bytes = csvData.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"monthly-attendance.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    // --- Employee Endpoints ---

    @GetMapping("/employee/attendance/today")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDTO>> getMyTodayAttendance(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return ResponseEntity.ok(ApiResponse.ok("Today's attendance retrieved",
                attendanceService.getTodayEmployeeAttendance(emp.getId()).orElse(null)));
    }

    @GetMapping("/employee/attendance/history")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getMyAttendanceHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return ResponseEntity.ok(ApiResponse.ok("Attendance history retrieved",
                attendanceService.getEmployeeAttendanceHistory(emp.getId())));
    }

    @GetMapping("/employee/attendance/events")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<com.company.employee_management.dto.DailyPunchSummaryDTO>> getMyDailyPunches(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        var summary = attendanceService.getDailyPunchSummary(emp.getId(), date);
        return ResponseEntity.ok(ApiResponse.ok("My punch events retrieved", summary));
    }

    @GetMapping("/employee/attendance/export-my-csv")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportMyAttendanceCsv(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        String csv = attendanceService.exportEmployeeAttendanceCsv(emp.getId(), month, year);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my-attendance-" + emp.getEmployeeCode() + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    @PostMapping("/employee/attendance/punch")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDTO>> employeePunch(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AttendancePunchRequest request) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        request.setEmployeeId(emp.getId());
        AttendanceDTO dto = attendanceService.recordAttendancePunch(request);
        return ResponseEntity.ok(ApiResponse.ok("Attendance recorded: " + request.getPunchType() + " successful", dto));
    }
}
