package com.company.employee_management.controller;

import com.company.employee_management.dto.ApiResponse;
import com.company.employee_management.dto.BreakActionRequest;
import com.company.employee_management.dto.BreakDTO;
import com.company.employee_management.entity.Employee;
import com.company.employee_management.exception.ResourceNotFoundException;
import com.company.employee_management.repository.EmployeeRepository;
import com.company.employee_management.security.UserPrincipal;
import com.company.employee_management.service.BreakService;
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

@RestController
@RequestMapping("/api")
public class BreakController {

    private final BreakService breakService;
    private final EmployeeRepository employeeRepository;

    public BreakController(BreakService breakService, EmployeeRepository employeeRepository) {
        this.breakService = breakService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/admin/breaks")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<BreakDTO>>> getBreaks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Page<BreakDTO> list = breakService.getBreaks(date, employeeId, PageRequest.of(page, size, Sort.by("breakDate").descending().and(Sort.by("breakStartTime").descending())));
        return ResponseEntity.ok(ApiResponse.ok("Breaks retrieved", list));
    }

    @GetMapping("/admin/breaks/export-csv")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportBreaksCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long employeeId) {
        String csvData = breakService.exportBreaksCsv(date, employeeId);
        byte[] bytes = csvData.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employee-breaks.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @PostMapping("/employee/breaks/action")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<BreakDTO>> breakAction(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BreakActionRequest request) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        request.setEmployeeId(emp.getId());
        BreakDTO dto = breakService.handleBreakAction(request, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Break " + request.getAction() + " registered", dto));
    }

    @GetMapping("/employee/breaks/my-breaks")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<BreakDTO>>> getMyBreaks(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return ResponseEntity.ok(ApiResponse.ok("Breaks retrieved", breakService.getEmployeeBreaks(emp.getId())));
    }
}
