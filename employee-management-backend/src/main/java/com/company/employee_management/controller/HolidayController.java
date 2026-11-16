package com.company.employee_management.controller;

import com.company.employee_management.dto.ApiResponse;
import com.company.employee_management.dto.HolidayDTO;
import com.company.employee_management.security.UserPrincipal;
import com.company.employee_management.service.HolidayService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping("/admin/holidays")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<HolidayDTO>>> getAdminHolidays(@RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.ok("Holidays retrieved", holidayService.getHolidays(year)));
    }

    @PostMapping("/admin/holidays")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<HolidayDTO>> addHoliday(
            @Valid @RequestBody HolidayDTO dto,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        HolidayDTO created = holidayService.addHoliday(dto, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Holiday added successfully", created));
    }

    @PutMapping("/admin/holidays/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<HolidayDTO>> updateHoliday(
            @PathVariable Long id,
            @RequestBody HolidayDTO dto,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        HolidayDTO updated = holidayService.updateHoliday(id, dto, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Holiday updated successfully", updated));
    }

    @DeleteMapping("/admin/holidays/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteHoliday(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        holidayService.deleteHoliday(id, adminUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Holiday deleted successfully", "Deleted id: " + id));
    }

    @GetMapping("/admin/holidays/export-csv")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportHolidaysCsv(@RequestParam(required = false) Integer year) {
        String csvData = holidayService.exportHolidaysCsv(year);
        byte[] bytes = csvData.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"company-holidays.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/employee/holidays")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<HolidayDTO>>> getEmployeeHolidays(@RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.ok("Holidays retrieved", holidayService.getHolidays(year)));
    }
}
