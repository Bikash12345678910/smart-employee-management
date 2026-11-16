package com.company.employee_management.controller;

import com.company.employee_management.dto.ApiResponse;
import com.company.employee_management.dto.EmployeeCreateRequest;
import com.company.employee_management.dto.EmployeeDTO;
import com.company.employee_management.dto.EmployeeUpdateRequest;
import com.company.employee_management.entity.Department;
import com.company.employee_management.entity.Designation;
import com.company.employee_management.entity.Employee;
import com.company.employee_management.exception.ResourceNotFoundException;
import com.company.employee_management.repository.DepartmentRepository;
import com.company.employee_management.repository.DesignationRepository;
import com.company.employee_management.repository.EmployeeRepository;
import com.company.employee_management.security.UserPrincipal;
import com.company.employee_management.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeService employeeService,
                              DepartmentRepository departmentRepository,
                              DesignationRepository designationRepository,
                              EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.employeeRepository = employeeRepository;
    }

    // --- Admin Endpoints ---

    @GetMapping("/admin/employees")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<EmployeeDTO>>> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<EmployeeDTO> result = employeeService.getEmployees(search, departmentId, status, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.ok("Employees retrieved", result));
    }

    @GetMapping("/admin/employees/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployeeById(@PathVariable Long id) {
        EmployeeDTO dto = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.ok("Employee details retrieved", dto));
    }

    @PostMapping("/admin/employees")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        EmployeeDTO created = employeeService.createEmployee(request, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Employee registered successfully", created));
    }

    @PutMapping("/admin/employees/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        EmployeeDTO updated = employeeService.updateEmployee(id, request, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Employee updated successfully", updated));
    }

    @DeleteMapping("/admin/employees/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteEmployee(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        employeeService.deleteEmployee(id, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Employee deleted successfully", "Deleted id: " + id));
    }

    @PatchMapping("/admin/employees/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> toggleEmployeeStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        EmployeeDTO updated = employeeService.toggleActiveStatus(id, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Employee status updated", updated));
    }

    @GetMapping("/admin/employees/export-csv")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportEmployeesCsv() {
        String csvData = employeeService.exportEmployeesToCsv();
        byte[] bytes = csvData.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/admin/employees/departments")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments() {
        return ResponseEntity.ok(ApiResponse.ok("Departments retrieved", departmentRepository.findAll()));
    }

    @GetMapping("/admin/employees/designations")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<Designation>>> getDesignations(@RequestParam(required = false) Long departmentId) {
        List<Designation> list = (departmentId != null) ?
                designationRepository.findByDepartmentId(departmentId) : designationRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Designations retrieved", list));
    }

    // --- Employee Portal Self-Service ---

    @GetMapping("/employee/profile")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for current user"));
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved", employeeService.mapToDTO(emp)));
    }

    @PutMapping("/employee/profile")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody EmployeeUpdateRequest request) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for current user"));

        // Employee can only update phone, address, photo url
        EmployeeUpdateRequest safeReq = new EmployeeUpdateRequest();
        safeReq.setPhoneNumber(request.getPhoneNumber());
        safeReq.setAddress(request.getAddress());
        safeReq.setProfilePhotoUrl(request.getProfilePhotoUrl());

        EmployeeDTO updated = employeeService.updateEmployee(emp.getId(), safeReq, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
    }
}
