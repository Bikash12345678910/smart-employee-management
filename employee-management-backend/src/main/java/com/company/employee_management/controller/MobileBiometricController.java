package com.company.employee_management.controller;

import com.company.employee_management.dto.ApiResponse;
import com.company.employee_management.dto.BiometricDeviceDTO;
import com.company.employee_management.dto.MobileBiometricSettingsDTO;
import com.company.employee_management.dto.MobileDeviceCreateRequest;
import com.company.employee_management.entity.BiometricVerificationLog;
import com.company.employee_management.entity.DeviceActivityLog;
import com.company.employee_management.security.UserPrincipal;
import com.company.employee_management.service.MobileBiometricSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MobileBiometricController {

    private final MobileBiometricSettingsService settingsService;

    public MobileBiometricController(MobileBiometricSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    // 1. Settings Endpoints
    @GetMapping("/admin/mobile-biometrics/settings")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MobileBiometricSettingsDTO>> getSettings() {
        return ResponseEntity.ok(ApiResponse.ok("Mobile biometric settings retrieved", settingsService.getSettings()));
    }

    @PutMapping("/admin/mobile-biometrics/settings")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MobileBiometricSettingsDTO>> updateSettings(
            @RequestBody MobileBiometricSettingsDTO dto,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        MobileBiometricSettingsDTO updated = settingsService.updateSettings(dto, adminUser != null ? adminUser.getUsername() : "admin");
        return ResponseEntity.ok(ApiResponse.ok("Mobile biometric settings updated successfully", updated));
    }

    @PostMapping("/admin/mobile-biometrics/settings/reset")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MobileBiometricSettingsDTO>> resetSettings(
            @AuthenticationPrincipal UserPrincipal adminUser) {
        MobileBiometricSettingsDTO reset = settingsService.resetToDefault(adminUser != null ? adminUser.getUsername() : "admin");
        return ResponseEntity.ok(ApiResponse.ok("Settings reset to default factory policy", reset));
    }

    // 2. Dashboard Overview Summary
    @GetMapping("/admin/mobile-biometrics/dashboard-summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary() {
        return ResponseEntity.ok(ApiResponse.ok("Dashboard summary retrieved", settingsService.getDashboardSummary()));
    }

    // 3. Device Management
    @GetMapping("/admin/mobile-biometrics/devices")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<BiometricDeviceDTO>>> getDevices() {
        return ResponseEntity.ok(ApiResponse.ok("Devices retrieved", settingsService.getAllDevices()));
    }

    @PostMapping("/admin/mobile-biometrics/devices")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<BiometricDeviceDTO>> registerDevice(
            @Valid @RequestBody MobileDeviceCreateRequest req,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        BiometricDeviceDTO dev = settingsService.registerDevice(req, adminUser != null ? adminUser.getUsername() : "admin");
        return ResponseEntity.ok(ApiResponse.ok("Device registered successfully", dev));
    }

    @PutMapping("/admin/mobile-biometrics/devices/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<BiometricDeviceDTO>> updateDeviceStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        BiometricDeviceDTO dev = settingsService.updateDeviceStatus(id, status, adminUser != null ? adminUser.getUsername() : "admin");
        return ResponseEntity.ok(ApiResponse.ok("Device status updated", dev));
    }

    @DeleteMapping("/admin/mobile-biometrics/devices/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeDevice(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        settingsService.deleteDevice(id, adminUser != null ? adminUser.getUsername() : "admin");
        return ResponseEntity.ok(ApiResponse.ok("Device authorization revoked successfully", null));
    }

    @PostMapping("/admin/mobile-biometrics/devices/{id}/test-connection")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testDeviceConnection(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Device connection test response", settingsService.testConnection(id)));
    }

    @GetMapping("/admin/mobile-biometrics/devices/{id}/logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<DeviceActivityLog>>> getDeviceLogs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Device activity logs retrieved", settingsService.getDeviceActivityLogs(id)));
    }

    // 4. Employee Biometric Registrations
    @GetMapping("/admin/mobile-biometrics/employee-registrations")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEmployeeRegistrations() {
        return ResponseEntity.ok(ApiResponse.ok("Employee registrations retrieved", settingsService.getEmployeeRegistrations()));
    }

    @PostMapping("/admin/mobile-biometrics/employees/{id}/reset-biometrics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetEmployeeBiometrics(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        return ResponseEntity.ok(ApiResponse.ok("Biometrics reset", settingsService.resetEmployeeBiometrics(id, adminUser != null ? adminUser.getUsername() : "admin")));
    }

    // 5. Verification & Activity Logs
    @GetMapping("/admin/mobile-biometrics/verification-logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<BiometricVerificationLog>>> getVerificationLogs(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(ApiResponse.ok("Verification logs retrieved", settingsService.getVerificationLogs(type)));
    }

    @GetMapping("/admin/mobile-biometrics/device-logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<DeviceActivityLog>>> getAllDeviceLogs() {
        return ResponseEntity.ok(ApiResponse.ok("All device logs retrieved", settingsService.getDeviceActivityLogs(null)));
    }

    // 6. QR Code Generation
    @PostMapping("/admin/mobile-biometrics/generate-qr/{employeeId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateEmployeeQr(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.ok("Signed QR code generated", settingsService.generateEmployeeQrCode(employeeId)));
    }

    // 7. Synchronization
    @PostMapping("/admin/mobile-biometrics/sync-now")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncNow(@AuthenticationPrincipal UserPrincipal adminUser) {
        return ResponseEntity.ok(ApiResponse.ok("Sync completed", settingsService.triggerSync(adminUser != null ? adminUser.getUsername() : "admin")));
    }

    // 8. System Health Diagnostics
    @GetMapping("/admin/mobile-biometrics/system-health")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        return ResponseEntity.ok(ApiResponse.ok("System diagnostics retrieved", settingsService.runSystemHealthDiagnostics()));
    }

    // 9. Public Active Policy (for mobile kiosks / tablets to fetch active config)
    @GetMapping("/public/mobile-biometrics/active-policy")
    public ResponseEntity<ApiResponse<MobileBiometricSettingsDTO>> getActivePolicy() {
        return ResponseEntity.ok(ApiResponse.ok("Active mobile biometric policy", settingsService.getSettings()));
    }
}
