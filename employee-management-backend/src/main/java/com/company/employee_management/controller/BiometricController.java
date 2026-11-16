package com.company.employee_management.controller;

import com.company.employee_management.dto.ApiResponse;
import com.company.employee_management.dto.BiometricDeviceDTO;
import com.company.employee_management.dto.BiometricEnrollRequest;
import com.company.employee_management.entity.Employee;
import com.company.employee_management.exception.ResourceNotFoundException;
import com.company.employee_management.repository.EmployeeRepository;
import com.company.employee_management.security.UserPrincipal;
import com.company.employee_management.service.BiometricService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BiometricController {

    private final BiometricService biometricService;
    private final EmployeeRepository employeeRepository;

    public BiometricController(BiometricService biometricService, EmployeeRepository employeeRepository) {
        this.biometricService = biometricService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/admin/biometrics/devices")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<BiometricDeviceDTO>>> getDevices() {
        return ResponseEntity.ok(ApiResponse.ok("Biometric devices retrieved", biometricService.getAllDevices()));
    }

    @PostMapping("/admin/biometrics/devices")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<BiometricDeviceDTO>> createDevice(
            @RequestBody BiometricDeviceDTO request,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        BiometricDeviceDTO saved = biometricService.createDevice(request, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Device registered successfully", saved));
    }

    @PutMapping("/admin/biometrics/devices/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<BiometricDeviceDTO>> updateDevice(
            @PathVariable Long id,
            @RequestBody BiometricDeviceDTO request,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        BiometricDeviceDTO updated = biometricService.updateDevice(id, request, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Device updated successfully", updated));
    }

    @DeleteMapping("/admin/biometrics/devices/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteDevice(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        biometricService.deleteDevice(id, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Device deleted successfully", "Device " + id + " deleted"));
    }

    @PostMapping("/admin/biometrics/devices/{id}/test-connection")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testConnection(@PathVariable Long id) {
        Map<String, Object> result = biometricService.testDeviceConnection(id);
        return ResponseEntity.ok(ApiResponse.ok("Device connection test completed", result));
    }

    @PostMapping("/admin/biometrics/devices/{id}/ping")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> pingDevice(@PathVariable Long id) {
        boolean ok = biometricService.pingDevice(id);
        return ResponseEntity.ok(ApiResponse.ok("Device ping response received", ok));
    }

    @PostMapping("/admin/biometrics/enroll")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enrollBiometric(
            @Valid @RequestBody BiometricEnrollRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        Map<String, Object> result = biometricService.enrollBiometric(request, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Biometric enrolled successfully", result));
    }

    @GetMapping("/admin/biometrics/status/{employeeId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBiometricStatus(@PathVariable Long employeeId) {
        Map<String, Object> status = biometricService.getBiometricStatus(employeeId);
        return ResponseEntity.ok(ApiResponse.ok("Biometric status retrieved", status));
    }

    @GetMapping("/employee/biometrics/status")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyBiometricStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Employee emp = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        Map<String, Object> status = biometricService.getBiometricStatus(emp.getId());
        return ResponseEntity.ok(ApiResponse.ok("Biometric status retrieved", status));
    }
}
