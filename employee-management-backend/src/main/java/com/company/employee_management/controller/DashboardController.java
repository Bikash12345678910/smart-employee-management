package com.company.employee_management.controller;

import com.company.employee_management.dto.AdminDashboardStatsDTO;
import com.company.employee_management.dto.ApiResponse;
import com.company.employee_management.dto.EmployeeDashboardStatsDTO;
import com.company.employee_management.entity.Employee;
import com.company.employee_management.repository.EmployeeRepository;
import com.company.employee_management.security.UserPrincipal;
import com.company.employee_management.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;
    private final EmployeeRepository employeeRepository;

    public DashboardController(DashboardService dashboardService, EmployeeRepository employeeRepository) {
        this.dashboardService = dashboardService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/admin/dashboard/stats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardStatsDTO>> getAdminDashboardStats() {
        AdminDashboardStatsDTO stats = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Admin dashboard statistics retrieved", stats));
    }

    @GetMapping("/employee/dashboard/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDashboardStatsDTO>> getEmployeeDashboardStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseGet(() -> employeeRepository.findAll().stream().findFirst().orElse(null));

        if (emp == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No employee profile linked to current account"));
        }

        EmployeeDashboardStatsDTO stats = dashboardService.getEmployeeDashboardStats(emp.getId());
        return ResponseEntity.ok(ApiResponse.ok("Employee dashboard statistics retrieved", stats));
    }
}
