package com.company.employee_management.controller;

import com.company.employee_management.dto.*;
import com.company.employee_management.entity.AttendanceAuditLog;
import com.company.employee_management.entity.BiometricEnrollment;
import com.company.employee_management.service.TerminalAttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared POCO M2 Pro Terminal REST API Controller.
 * Protected by Terminal Device Authentication (X-Terminal-Id + X-Terminal-Key),
 * Anti-Replay Nonce/Timestamp headers, and Admin JWT for management endpoints.
 */
@RestController
@RequestMapping("/api/terminal")
public class TerminalController {

    private final TerminalAttendanceService terminalService;

    public TerminalController(TerminalAttendanceService terminalService) {
        this.terminalService = terminalService;
    }

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<TerminalConfigDTO>> getTerminalConfig(
            @RequestHeader(value = "X-Terminal-Id", required = false) String terminalId,
            @RequestHeader(value = "X-Terminal-Key", required = false) String terminalKey) {
        try {
            TerminalConfigDTO dto = terminalService.getTerminalConfig(terminalId, terminalKey);
            return ResponseEntity.ok(ApiResponse.ok("Terminal configuration loaded", dto));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<List<TerminalEmployeeDTO>>> getTerminalEmployees(
            @RequestHeader(value = "X-Terminal-Id", required = false) String terminalId,
            @RequestHeader(value = "X-Terminal-Key", required = false) String terminalKey) {
        try {
            List<TerminalEmployeeDTO> list = terminalService.getTerminalEmployees(terminalId, terminalKey);
            return ResponseEntity.ok(ApiResponse.ok("Terminal employees retrieved", list));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/attendance/punch")
    public ResponseEntity<ApiResponse<TerminalPunchResponse>> punchAttendance(
            @RequestHeader(value = "X-Terminal-Id", required = false) String terminalId,
            @RequestHeader(value = "X-Terminal-Key", required = false) String terminalKey,
            @RequestHeader(value = "X-Request-Nonce", required = false) String nonce,
            @RequestHeader(value = "X-Request-Timestamp", required = false) Long timestampMs,
            @RequestBody TerminalPunchRequest request,
            HttpServletRequest httpRequest) {
        try {
            TerminalPunchResponse resp = terminalService.processAttendancePunch(
                    terminalId, terminalKey, nonce, timestampMs, request, httpRequest.getRemoteAddr());
            return ResponseEntity.ok(ApiResponse.ok(resp.getMessage(), resp));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/biometric/enroll")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enrollBiometric(
            @RequestHeader(value = "X-Terminal-Id", required = false) String terminalId,
            @RequestHeader(value = "X-Terminal-Key", required = false) String terminalKey,
            @RequestBody BiometricEnrollmentRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            String adminName = authentication != null ? authentication.getName() : "admin";
            BiometricEnrollment saved = terminalService.enrollEmployeeFace(
                    terminalId, terminalKey, request, adminName, httpRequest.getRemoteAddr());

            Map<String, Object> summary = new HashMap<>();
            summary.put("enrollmentId", saved.getId());
            summary.put("employeeCode", saved.getEmployeeCode());
            summary.put("modality", saved.getModality());
            summary.put("consentGiven", saved.getConsentGiven());
            summary.put("consentTimestamp", saved.getConsentTimestamp().toString());
            summary.put("encrypted", true);
            return ResponseEntity.ok(ApiResponse.ok("Face biometric enrolled securely with AES-256 encryption", summary));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @DeleteMapping("/biometric/{employeeCode}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> revokeBiometric(
            @RequestHeader(value = "X-Terminal-Id", required = false) String terminalId,
            @RequestHeader(value = "X-Terminal-Key", required = false) String terminalKey,
            @PathVariable String employeeCode,
            HttpServletRequest httpRequest) {
        try {
            terminalService.revokeEmployeeFace(terminalId, terminalKey, employeeCode, httpRequest.getRemoteAddr());
            return ResponseEntity.ok(ApiResponse.ok("Biometric enrollment revoked for " + employeeCode, employeeCode));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/reports/daily")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DailyReportSummaryDTO>> getDailyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyReportSummaryDTO summary = terminalService.getDailyReport(date);
        return ResponseEntity.ok(ApiResponse.ok("Daily attendance report generated", summary));
    }

    @GetMapping("/reports/export-csv")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportDailyCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        String csv = terminalService.exportDailyReportCsv(target);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance-report-" + target + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceAuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.ok("Audit logs retrieved", terminalService.getRecentAuditLogs()));
    }
}
