package com.company.employee_management.controller;

import com.company.employee_management.dto.ApiResponse;
import com.company.employee_management.dto.CompanySettingDTO;
import com.company.employee_management.security.UserPrincipal;
import com.company.employee_management.service.CompanySettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CompanySettingsController {

    private final CompanySettingsService settingsService;

    public CompanySettingsController(CompanySettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/admin/settings")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CompanySettingDTO>> getSettings() {
        return ResponseEntity.ok(ApiResponse.ok("Company settings retrieved", settingsService.getSettings()));
    }

    @PutMapping("/admin/settings")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CompanySettingDTO>> updateSettings(
            @RequestBody CompanySettingDTO dto,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        CompanySettingDTO updated = settingsService.updateSettings(dto, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Company settings updated successfully", updated));
    }

    @GetMapping("/public/settings")
    public ResponseEntity<ApiResponse<CompanySettingDTO>> getPublicSettings() {
        return ResponseEntity.ok(ApiResponse.ok("Company public settings", settingsService.getSettings()));
    }
}
