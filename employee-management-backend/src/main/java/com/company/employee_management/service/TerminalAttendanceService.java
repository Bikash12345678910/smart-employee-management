package com.company.employee_management.service;

import com.company.employee_management.dto.*;
import com.company.employee_management.entity.*;
import com.company.employee_management.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TerminalAttendanceService {

    private static final String MODALITY_FACE = "FACE_CAMERAX";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AttendanceTerminalRepository terminalRepository;
    private final BiometricEnrollmentRepository enrollmentRepository;
    private final AttendanceAuditLogRepository auditLogRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final CompanySettingRepository companySettingRepository;
    private final BiometricCryptoService cryptoService;
    private final ReplayProtectionService replayProtectionService;

    public TerminalAttendanceService(
            AttendanceTerminalRepository terminalRepository,
            BiometricEnrollmentRepository enrollmentRepository,
            AttendanceAuditLogRepository auditLogRepository,
            EmployeeRepository employeeRepository,
            AttendanceRepository attendanceRepository,
            CompanySettingRepository companySettingRepository,
            BiometricCryptoService cryptoService,
            ReplayProtectionService replayProtectionService) {
        this.terminalRepository = terminalRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.companySettingRepository = companySettingRepository;
        this.cryptoService = cryptoService;
        this.replayProtectionService = replayProtectionService;
    }

    public TerminalConfigDTO getTerminalConfig(String terminalCode, String terminalKey) {
        AttendanceTerminal terminal = replayProtectionService.authenticateTerminal(terminalCode, terminalKey);
        String companyName = companySettingRepository.findAll().stream()
                .findFirst()
                .map(CompanySetting::getCompanyName)
                .orElse("Smart Employee Management System");

        TerminalConfigDTO dto = new TerminalConfigDTO();
        dto.setTerminalCode(terminal.getTerminalCode());
        dto.setTerminalName(terminal.getTerminalName());
        dto.setDeviceModel(terminal.getDeviceModel());
        dto.setCompanyName(companyName);
        dto.setTapTimeoutMs(terminal.getTapTimeoutMs());
        dto.setFaceMatchThreshold(terminal.getFaceMatchThreshold().doubleValue());
        dto.setLivenessRequired(terminal.getLivenessRequired());
        dto.setBiometricPromptEnabled(terminal.getBiometricPromptEnabled());
        dto.setKioskModeEnabled(terminal.getKioskModeEnabled());
        dto.setServerTimeEpochMs(Instant.now().toEpochMilli());
        return dto;
    }

    public List<TerminalEmployeeDTO> getTerminalEmployees(String terminalCode, String terminalKey) {
        replayProtectionService.authenticateTerminal(terminalCode, terminalKey);
        LocalDate today = LocalDate.now();
        List<Employee> employees = employeeRepository.findAll();
        List<Attendance> todayAttendances = attendanceRepository.findByAttendanceDate(today);
        Map<Long, Attendance> attendanceMap = new HashMap<>();
        for (Attendance a : todayAttendances) {
            attendanceMap.put(a.getEmployee().getId(), a);
        }

        List<BiometricEnrollment> activeEnrollments = enrollmentRepository.findByIsActiveTrue();
        Map<Long, BiometricEnrollment> enrollmentMap = new HashMap<>();
        for (BiometricEnrollment e : activeEnrollments) {
            if (MODALITY_FACE.equals(e.getModality())) {
                enrollmentMap.put(e.getEmployee().getId(), e);
            }
        }

        List<TerminalEmployeeDTO> result = new ArrayList<>();
        for (Employee emp : employees) {
            TerminalEmployeeDTO dto = new TerminalEmployeeDTO();
            dto.setId(emp.getId());
            dto.setEmployeeCode(emp.getEmployeeCode());
            dto.setFullName(emp.getFirstName() + " " + emp.getLastName());
            dto.setEmail(emp.getEmail());
            dto.setPhoneNumber(emp.getPhoneNumber());
            dto.setIsActive(Boolean.TRUE.equals(emp.getIsActive()));

            BiometricEnrollment enroll = enrollmentMap.get(emp.getId());
            dto.setFaceEnrolled(enroll != null);
            dto.setConsentGiven(enroll != null && Boolean.TRUE.equals(enroll.getConsentGiven()));

            Attendance att = attendanceMap.get(emp.getId());
            if (att == null || att.getCheckInTime() == null) {
                dto.setTodayStatus("NOT_CHECKED_IN");
            } else if (att.getCheckOutTime() == null) {
                dto.setTodayStatus("CHECKED_IN");
                dto.setCheckInTime(att.getCheckInTime().format(TIME_FMT));
            } else {
                dto.setTodayStatus("CHECKED_OUT");
                dto.setCheckInTime(att.getCheckInTime().format(TIME_FMT));
                dto.setCheckOutTime(att.getCheckOutTime().format(TIME_FMT));
            }
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public BiometricEnrollment enrollEmployeeFace(
            String terminalCode, String terminalKey,
            BiometricEnrollmentRequest req, String adminUsername, String clientIp) {
        AttendanceTerminal terminal = replayProtectionService.authenticateTerminal(terminalCode, terminalKey);

        if (!Boolean.TRUE.equals(req.getConsentGiven())) {
            recordAudit(terminal.getTerminalCode(), null, req.getEmployeeCode(), "FACE_ENROLL",
                    "FACE_CAMERAX", null, null, "REJECTED", "Explicit biometric consent not provided", null, clientIp);
            throw new IllegalArgumentException("Explicit employee biometric consent is required before enrollment.");
        }

        if (req.getFaceVector() == null || req.getFaceVector().length < 16) {
            throw new IllegalArgumentException("Invalid or incomplete face feature vector captured.");
        }

        Employee emp = employeeRepository.findByEmployeeCode(req.getEmployeeCode())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + req.getEmployeeCode()));

        if (!Boolean.TRUE.equals(emp.getIsActive())) {
            throw new IllegalArgumentException("Cannot enroll biometric data for inactive employee.");
        }

        String encryptedTemplate = cryptoService.encryptTemplate(req.getFaceVector());

        BiometricEnrollment enrollment = enrollmentRepository
                .findByEmployeeIdAndModality(emp.getId(), MODALITY_FACE)
                .orElseGet(BiometricEnrollment::new);

        enrollment.setEmployee(emp);
        enrollment.setEmployeeCode(emp.getEmployeeCode());
        enrollment.setModality(MODALITY_FACE);
        enrollment.setEncryptedTemplate(encryptedTemplate);
        enrollment.setTemplateVersion("MLKIT_V1_28D");
        enrollment.setQualityScore(BigDecimal.valueOf(req.getQualityScore() != null ? req.getQualityScore() : 0.92));
        enrollment.setConsentGiven(true);
        enrollment.setConsentTextVersion(req.getConsentTextVersion() != null ? req.getConsentTextVersion() : "CONSENT_V1.0");
        enrollment.setConsentTimestamp(LocalDateTime.now());
        enrollment.setEnrolledByAdmin(adminUsername != null ? adminUsername : "admin");
        enrollment.setTerminalCode(terminal.getTerminalCode());
        enrollment.setIsActive(true);

        BiometricEnrollment saved = enrollmentRepository.save(enrollment);

        recordAudit(terminal.getTerminalCode(), emp.getId(), emp.getEmployeeCode(), "FACE_ENROLL",
                "FACE_CAMERAX", true, BigDecimal.ONE, "SUCCESS", null, null, clientIp);

        return saved;
    }

    @Transactional
    public void revokeEmployeeFace(String terminalCode, String terminalKey, String employeeCode, String clientIp) {
        AttendanceTerminal terminal = replayProtectionService.authenticateTerminal(terminalCode, terminalKey);
        BiometricEnrollment enrollment = enrollmentRepository
                .findByEmployeeCodeAndModalityAndIsActiveTrue(employeeCode, MODALITY_FACE)
                .orElseThrow(() -> new IllegalArgumentException("No active biometric enrollment for: " + employeeCode));

        enrollment.setIsActive(false);
        enrollmentRepository.save(enrollment);

        recordAudit(terminal.getTerminalCode(), enrollment.getEmployee().getId(), employeeCode,
                "FACE_REVOKE", "ADMIN_JWT", null, null, "SUCCESS", null, null, clientIp);
    }

    /**
     * Core Secure Attendance Punch (1-Tap CHECK_IN / 4-Tap CHECK_OUT).
     * Never trusts employee ID alone: enforces Terminal Auth, Replay Protection,
     * Liveness Check, 1:1 Encrypted Face Matching, and Server-Side Timestamps.
     */
    @Transactional
    public TerminalPunchResponse processAttendancePunch(
            String terminalCode, String terminalKey,
            String headerNonce, Long headerTimestampMs,
            TerminalPunchRequest req, String clientIp) {

        AttendanceTerminal terminal = replayProtectionService.authenticateTerminal(terminalCode, terminalKey);

        String effectiveNonce = (headerNonce != null && !headerNonce.isBlank()) ? headerNonce : req.getNonce();
        Long effectiveClientTs = headerTimestampMs != null ? headerTimestampMs : req.getClientTimestampMs();

        // 1. Replay Attack Protection
        try {
            replayProtectionService.validateReplayProtection(effectiveNonce, effectiveClientTs);
        } catch (SecurityException ex) {
            recordAudit(terminal.getTerminalCode(), null, req.getEmployeeCode(), "REPLAY_BLOCKED",
                    req.getVerificationMethod(), req.getLivenessPassed(), null, "REJECTED",
                    ex.getMessage(), effectiveNonce, clientIp);
            throw ex;
        }

        // 2. Validate Action
        String action = req.getAction() != null ? req.getAction().trim().toUpperCase() : "";
        if (!"CHECK_IN".equals(action) && !"CHECK_OUT".equals(action)) {
            throw new IllegalArgumentException("Invalid attendance action. Must be CHECK_IN or CHECK_OUT.");
        }

        // 3. Employee Lookup & Active Check
        Employee emp = employeeRepository.findByEmployeeCode(req.getEmployeeCode())
                .orElseThrow(() -> {
                    recordAudit(terminal.getTerminalCode(), null, req.getEmployeeCode(), action,
                            req.getVerificationMethod(), req.getLivenessPassed(), null, "FAILED",
                            "Employee code not found", effectiveNonce, clientIp);
                    return new IllegalArgumentException("Employee not found: " + req.getEmployeeCode());
                });

        if (!Boolean.TRUE.equals(emp.getIsActive())) {
            recordAudit(terminal.getTerminalCode(), emp.getId(), emp.getEmployeeCode(), action,
                    req.getVerificationMethod(), req.getLivenessPassed(), null, "REJECTED",
                    "Employee account is deactivated", effectiveNonce, clientIp);
            throw new IllegalArgumentException("Employee account is deactivated.");
        }

        // 4. Anti-Spoofing & Liveness Check
        if (Boolean.TRUE.equals(terminal.getLivenessRequired()) && !Boolean.TRUE.equals(req.getLivenessPassed())) {
            recordAudit(terminal.getTerminalCode(), emp.getId(), emp.getEmployeeCode(), action,
                    req.getVerificationMethod(), false, BigDecimal.ZERO, "REJECTED",
                    "Liveness verification failed (possible photo/video spoofing)", effectiveNonce, clientIp);
            throw new SecurityException("Liveness check failed. Please look directly at the front camera and blink.");
        }

        // 5. 1:1 Face Biometric Verification (Do NOT trust Employee ID alone)
        BiometricEnrollment enrollment = enrollmentRepository
                .findByEmployeeIdAndModalityAndIsActiveTrue(emp.getId(), MODALITY_FACE)
                .orElseThrow(() -> {
                    recordAudit(terminal.getTerminalCode(), emp.getId(), emp.getEmployeeCode(), action,
                            req.getVerificationMethod(), req.getLivenessPassed(), BigDecimal.ZERO, "FAILED",
                            "No face template enrolled", effectiveNonce, clientIp);
                    return new IllegalStateException("Face biometric not enrolled for " + emp.getEmployeeCode() + ". Please contact Admin.");
                });

        if (!Boolean.TRUE.equals(enrollment.getConsentGiven())) {
            throw new SecurityException("Biometric consent is missing for this employee.");
        }

        double[] enrolledVector = cryptoService.decryptTemplate(enrollment.getEncryptedTemplate());
        double matchScore = cryptoService.calculateSimilarity(enrolledVector, req.getFaceVector());
        double threshold = terminal.getFaceMatchThreshold().doubleValue();

        if (matchScore < threshold) {
            recordAudit(terminal.getTerminalCode(), emp.getId(), emp.getEmployeeCode(), action,
                    req.getVerificationMethod(), req.getLivenessPassed(), BigDecimal.valueOf(matchScore), "REJECTED",
                    String.format("Face mismatch (score %.4f < threshold %.4f)", matchScore, threshold),
                    effectiveNonce, clientIp);
            throw new SecurityException(String.format(
                    "Face verification failed (match %.1f%% < required %.1f%%). Identity does not match %s.",
                    matchScore * 100.0, threshold * 100.0, emp.getFirstName()));
        }

        // 6. Server-Side Attendance Timestamps & Strict Duplicate Prevention
        LocalDate serverDate = LocalDate.now();
        LocalTime serverTime = LocalTime.now();
        LocalDateTime serverNow = LocalDateTime.now();

        Optional<Attendance> existingOpt = attendanceRepository.findByEmployeeIdAndAttendanceDate(emp.getId(), serverDate);
        Attendance attendance;

        CompanySetting settings = companySettingRepository.findAll().stream().findFirst().orElse(null);
        LocalTime shiftStart = (settings != null && settings.getShiftStartTime() != null)
                ? settings.getShiftStartTime() : LocalTime.of(9, 0);
        int graceMinutes = (settings != null && settings.getLateArrivalGraceMinutes() != null)
                ? settings.getLateArrivalGraceMinutes() : 15;

        if ("CHECK_IN".equals(action)) {
            if (existingOpt.isPresent() && existingOpt.get().getCheckInTime() != null) {
                recordAudit(terminal.getTerminalCode(), emp.getId(), emp.getEmployeeCode(), action,
                        req.getVerificationMethod(), true, BigDecimal.valueOf(matchScore), "REJECTED",
                        "Duplicate check-in prevented", effectiveNonce, clientIp);
                throw new IllegalStateException("Already checked in today at "
                        + existingOpt.get().getCheckInTime().format(TIME_FMT) + ". Duplicate check-in is blocked.");
            }

            attendance = existingOpt.orElseGet(Attendance::new);
            attendance.setEmployee(emp);
            attendance.setAttendanceDate(serverDate);
            attendance.setCheckInTime(serverTime);
            attendance.setAttendanceVia(Boolean.TRUE.equals(req.getBiometricPromptUsed())
                    ? "FACE_AND_FINGERPRINT" : "FACE_RECOGNITION");

            LocalTime lateCutoff = shiftStart.plusMinutes(graceMinutes);
            if (serverTime.isAfter(lateCutoff)) {
                int lateMins = (int) Duration.between(shiftStart, serverTime).toMinutes();
                attendance.setLateMinutes(lateMins);
                attendance.setAttendanceStatus("LATE");
            } else {
                attendance.setLateMinutes(0);
                attendance.setAttendanceStatus("PRESENT");
            }
            attendanceRepository.save(attendance);
        } else {
            // CHECK_OUT
            if (existingOpt.isEmpty() || existingOpt.get().getCheckInTime() == null) {
                recordAudit(terminal.getTerminalCode(), emp.getId(), emp.getEmployeeCode(), action,
                        req.getVerificationMethod(), true, BigDecimal.valueOf(matchScore), "REJECTED",
                        "Check-out attempted without check-in", effectiveNonce, clientIp);
                throw new IllegalStateException("Cannot check out without checking in first today.");
            }

            attendance = existingOpt.get();
            if (attendance.getCheckOutTime() != null) {
                recordAudit(terminal.getTerminalCode(), emp.getId(), emp.getEmployeeCode(), action,
                        req.getVerificationMethod(), true, BigDecimal.valueOf(matchScore), "REJECTED",
                        "Duplicate check-out prevented", effectiveNonce, clientIp);
                throw new IllegalStateException("Already checked out today at "
                        + attendance.getCheckOutTime().format(TIME_FMT) + ". Duplicate check-out is blocked.");
            }

            attendance.setCheckOutTime(serverTime);
            long totalMins = Math.max(0, Duration.between(attendance.getCheckInTime(), serverTime).toMinutes());
            attendance.setWorkingMinutes((int) totalMins);
            attendance.setWorkingHours((int) (totalMins / 60));
            attendanceRepository.save(attendance);
        }

        // 7. Record Success Audit Log
        recordAudit(terminal.getTerminalCode(), emp.getId(), emp.getEmployeeCode(), action,
                req.getVerificationMethod() != null ? req.getVerificationMethod() : "FACE_LIVENESS",
                true, BigDecimal.valueOf(matchScore), "SUCCESS", null, effectiveNonce, clientIp);

        TerminalPunchResponse resp = new TerminalPunchResponse();
        resp.setVerified(true);
        resp.setAction(action);
        resp.setEmployeeCode(emp.getEmployeeCode());
        resp.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());
        resp.setAttendanceDate(serverDate.toString());
        resp.setServerTimestamp(serverNow.format(DT_FMT));
        resp.setCheckInTime(attendance.getCheckInTime() != null ? attendance.getCheckInTime().format(TIME_FMT) : null);
        resp.setCheckOutTime(attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().format(TIME_FMT) : null);
        resp.setWorkingMinutes(attendance.getWorkingMinutes() != null ? attendance.getWorkingMinutes().longValue() : 0L);
        resp.setMatchScore(matchScore);
        resp.setVerificationStatus("VERIFIED");
        resp.setMessage("CHECK_IN".equals(action)
                ? "Check-In recorded at " + serverTime.format(TIME_FMT)
                : "Check-Out recorded at " + serverTime.format(TIME_FMT) + " (" + resp.getWorkingMinutes() + " mins)");
        return resp;
    }

    public DailyReportSummaryDTO getDailyReport(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .toList();
        List<Attendance> attendances = attendanceRepository.findByAttendanceDate(targetDate);
        Map<Long, Attendance> attMap = new HashMap<>();
        for (Attendance a : attendances) {
            attMap.put(a.getEmployee().getId(), a);
        }

        int present = 0, absent = 0, late = 0, checkedOut = 0;
        List<DailyReportSummaryDTO.DailyReportRow> rows = new ArrayList<>();

        for (Employee emp : activeEmployees) {
            Attendance att = attMap.get(emp.getId());
            DailyReportSummaryDTO.DailyReportRow row = new DailyReportSummaryDTO.DailyReportRow();
            row.setEmployeeCode(emp.getEmployeeCode());
            row.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());

            if (att == null || att.getCheckInTime() == null) {
                absent++;
                row.setStatus("ABSENT");
                row.setWorkingMinutes(0L);
            } else {
                present++;
                if ("LATE".equalsIgnoreCase(att.getAttendanceStatus())) {
                    late++;
                    row.setStatus("LATE");
                } else {
                    row.setStatus("PRESENT");
                }
                row.setCheckInTime(att.getCheckInTime().format(TIME_FMT));
                if (att.getCheckOutTime() != null) {
                    checkedOut++;
                    row.setCheckOutTime(att.getCheckOutTime().format(TIME_FMT));
                }
                row.setWorkingMinutes(att.getWorkingMinutes() != null ? att.getWorkingMinutes().longValue() : 0L);
            }
            rows.add(row);
        }

        DailyReportSummaryDTO summary = new DailyReportSummaryDTO();
        summary.setDate(targetDate.toString());
        summary.setTotalEmployees(activeEmployees.size());
        summary.setPresentCount(present);
        summary.setAbsentCount(absent);
        summary.setLateCount(late);
        summary.setCheckedOutCount(checkedOut);
        summary.setRecords(rows);
        return summary;
    }

    public String exportDailyReportCsv(LocalDate date) {
        DailyReportSummaryDTO report = getDailyReport(date);
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Employee Code,Employee Name,Status,Check-In Time,Check-Out Time,Working Minutes\n");
        for (DailyReportSummaryDTO.DailyReportRow r : report.getRecords()) {
            sb.append(report.getDate()).append(',')
              .append(r.getEmployeeCode()).append(',')
              .append('"').append(r.getEmployeeName()).append('"').append(',')
              .append(r.getStatus()).append(',')
              .append(r.getCheckInTime() != null ? r.getCheckInTime() : "").append(',')
              .append(r.getCheckOutTime() != null ? r.getCheckOutTime() : "").append(',')
              .append(r.getWorkingMinutes() != null ? r.getWorkingMinutes() : 0).append('\n');
        }
        return sb.toString();
    }

    public List<AttendanceAuditLog> getRecentAuditLogs() {
        return auditLogRepository.findTop100ByOrderByServerTimestampDesc();
    }

    private void recordAudit(
            String terminalCode, Long employeeId, String employeeCode,
            String actionType, String verificationMethod, Boolean livenessPassed,
            BigDecimal matchScore, String status, String failureReason,
            String nonce, String clientIp) {
        AttendanceAuditLog log = new AttendanceAuditLog();
        log.setTerminalCode(terminalCode);
        log.setEmployeeId(employeeId);
        log.setEmployeeCode(employeeCode);
        log.setActionType(actionType);
        log.setVerificationMethod(verificationMethod);
        log.setLivenessPassed(livenessPassed);
        log.setMatchScore(matchScore);
        log.setStatus(status);
        log.setFailureReason(failureReason);
        log.setRequestNonce(nonce);
        log.setClientIp(clientIp);
        log.setServerTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
