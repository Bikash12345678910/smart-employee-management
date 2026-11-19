package com.company.employee_management.service;

import com.company.employee_management.entity.AttendanceTerminal;
import com.company.employee_management.repository.AttendanceAuditLogRepository;
import com.company.employee_management.repository.AttendanceTerminalRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces Terminal Device Authentication and Anti-Replay Protection.
 * Validates X-Terminal-Id, X-Terminal-Key, X-Request-Nonce, and X-Request-Timestamp.
 */
@Service
public class ReplayProtectionService {

    private static final long MAX_TIMESTAMP_DRIFT_MS = 180_000L; // 3 minutes window
    private final Map<String, Long> nonceCache = new ConcurrentHashMap<>();

    private final AttendanceTerminalRepository terminalRepository;
    private final AttendanceAuditLogRepository auditLogRepository;

    public ReplayProtectionService(
            AttendanceTerminalRepository terminalRepository,
            AttendanceAuditLogRepository auditLogRepository) {
        this.terminalRepository = terminalRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public AttendanceTerminal authenticateTerminal(String terminalCode, String terminalKey) {
        if (terminalCode == null || terminalCode.isBlank() || terminalKey == null || terminalKey.isBlank()) {
            throw new SecurityException("Missing terminal authentication headers (X-Terminal-Id / X-Terminal-Key)");
        }

        AttendanceTerminal terminal = terminalRepository.findByTerminalCode(terminalCode.trim())
                .orElseThrow(() -> new SecurityException("Unregistered attendance terminal: " + terminalCode));

        if (!Boolean.TRUE.equals(terminal.getIsActive())) {
            throw new SecurityException("Attendance terminal is deactivated: " + terminalCode);
        }

        if (!terminal.getApiKeyHash().equals(terminalKey.trim())) {
            throw new SecurityException("Invalid terminal authentication key for: " + terminalCode);
        }

        terminal.setLastSeenAt(LocalDateTime.now());
        terminalRepository.save(terminal);
        return terminal;
    }

    public void validateReplayProtection(String nonce, Long requestTimestampMs) {
        if (nonce == null || nonce.trim().length() < 8) {
            throw new SecurityException("Missing or invalid replay-protection nonce");
        }

        long nowMs = Instant.now().toEpochMilli();
        if (requestTimestampMs != null) {
            long drift = Math.abs(nowMs - requestTimestampMs);
            if (drift > MAX_TIMESTAMP_DRIFT_MS) {
                throw new SecurityException("Request expired or clock drift exceeded (" + (drift / 1000) + "s)");
            }
        }

        // Prune expired nonces from memory
        nonceCache.entrySet().removeIf(entry -> (nowMs - entry.getValue()) > MAX_TIMESTAMP_DRIFT_MS * 2);

        String normalizedNonce = nonce.trim();
        if (nonceCache.putIfAbsent(normalizedNonce, nowMs) != null
                || auditLogRepository.existsByRequestNonce(normalizedNonce)) {
            throw new SecurityException("Replay attack detected: request nonce already used");
        }
    }
}
