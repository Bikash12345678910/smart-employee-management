package com.company.employee_management.controller;

import com.company.employee_management.dto.ApiResponse;
import com.company.employee_management.dto.GeneratePayslipRequest;
import com.company.employee_management.dto.PayslipDTO;
import com.company.employee_management.entity.Employee;
import com.company.employee_management.exception.BadRequestException;
import com.company.employee_management.exception.ResourceNotFoundException;
import com.company.employee_management.repository.EmployeeRepository;
import com.company.employee_management.security.UserPrincipal;
import com.company.employee_management.service.PayslipService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PayslipController {

    private final PayslipService payslipService;
    private final EmployeeRepository employeeRepository;

    public PayslipController(PayslipService payslipService, EmployeeRepository employeeRepository) {
        this.payslipService = payslipService;
        this.employeeRepository = employeeRepository;
    }

    // --- Admin Endpoints ---

    @GetMapping("/admin/payslips")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<PayslipDTO>>> getPayslips(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PayslipDTO> list = payslipService.getPayslips(month, year, employeeId,
                PageRequest.of(page, size, Sort.by("salaryYear").descending().and(Sort.by("salaryMonth").descending())));
        return ResponseEntity.ok(ApiResponse.ok("Payslips retrieved", list));
    }

    @GetMapping("/admin/payslips/salary-calculations")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<com.company.employee_management.dto.SalaryCalculationDTO>>> getSalaryCalculations(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        List<com.company.employee_management.dto.SalaryCalculationDTO> list = payslipService.getSalaryCalculations(month, year);
        return ResponseEntity.ok(ApiResponse.ok("Salary calculation table retrieved", list));
    }

    @GetMapping("/admin/payslips/export-csv")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportPayslipsCsv(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        String csv = payslipService.exportPayslipsCsv(month, year);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"payslips-" + (year != null ? year : 2026) + "-" + (month != null ? month : 11) + ".csv\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    @PostMapping("/admin/payslips/generate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PayslipDTO>> generatePayslip(
            @Valid @RequestBody GeneratePayslipRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        PayslipDTO dto = payslipService.generatePayslip(request, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Payslip generated successfully", dto));
    }

    @GetMapping("/admin/payslips/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PayslipDTO>> getPayslipById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Payslip retrieved", payslipService.getPayslipById(id)));
    }

    // --- Employee Endpoints ---

    @GetMapping("/employee/payslips")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<PayslipDTO>>> getMyPayslips(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return ResponseEntity.ok(ApiResponse.ok("Payslips retrieved", payslipService.getEmployeePayslips(emp.getId())));
    }

    @GetMapping("/employee/payslips/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PayslipDTO>> getMyPayslipById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        PayslipDTO p = payslipService.getPayslipById(id);
        if (!p.getEmployeeId().equals(emp.getId()) && !userPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new BadRequestException("Access denied: You can only view your own payslips");
        }
        return ResponseEntity.ok(ApiResponse.ok("Payslip retrieved", p));
    }
}
