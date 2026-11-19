package com.company.employee_management.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_terminals")
public class AttendanceTerminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_code", nullable = false, unique = true, length = 64)
    private String terminalCode;

    @Column(name = "terminal_name", nullable = false, length = 120)
    private String terminalName;

    @Column(name = "device_model", nullable = false, length = 100)
    private String deviceModel = "Xiaomi POCO M2 Pro";

    @Column(name = "location", nullable = false, length = 150)
    private String location = "Main Office Entrance";

    @Column(name = "api_key_hash", nullable = false)
    private String apiKeyHash;

    @Column(name = "hmac_secret", nullable = false)
    private String hmacSecret;

    @Column(name = "tap_timeout_ms", nullable = false)
    private Integer tapTimeoutMs = 1400;

    @Column(name = "face_match_threshold", nullable = false, precision = 5, scale = 4)
    private BigDecimal faceMatchThreshold = new BigDecimal("0.8200");

    @Column(name = "liveness_required", nullable = false)
    private Boolean livenessRequired = true;

    @Column(name = "biometric_prompt_enabled", nullable = false)
    private Boolean biometricPromptEnabled = true;

    @Column(name = "kiosk_mode_enabled", nullable = false)
    private Boolean kioskModeEnabled = true;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public AttendanceTerminal() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTerminalCode() { return terminalCode; }
    public void setTerminalCode(String terminalCode) { this.terminalCode = terminalCode; }

    public String getTerminalName() { return terminalName; }
    public void setTerminalName(String terminalName) { this.terminalName = terminalName; }

    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getApiKeyHash() { return apiKeyHash; }
    public void setApiKeyHash(String apiKeyHash) { this.apiKeyHash = apiKeyHash; }

    public String getHmacSecret() { return hmacSecret; }
    public void setHmacSecret(String hmacSecret) { this.hmacSecret = hmacSecret; }

    public Integer getTapTimeoutMs() { return tapTimeoutMs; }
    public void setTapTimeoutMs(Integer tapTimeoutMs) { this.tapTimeoutMs = tapTimeoutMs; }

    public BigDecimal getFaceMatchThreshold() { return faceMatchThreshold; }
    public void setFaceMatchThreshold(BigDecimal faceMatchThreshold) { this.faceMatchThreshold = faceMatchThreshold; }

    public Boolean getLivenessRequired() { return livenessRequired; }
    public void setLivenessRequired(Boolean livenessRequired) { this.livenessRequired = livenessRequired; }

    public Boolean getBiometricPromptEnabled() { return biometricPromptEnabled; }
    public void setBiometricPromptEnabled(Boolean biometricPromptEnabled) { this.biometricPromptEnabled = biometricPromptEnabled; }

    public Boolean getKioskModeEnabled() { return kioskModeEnabled; }
    public void setKioskModeEnabled(Boolean kioskModeEnabled) { this.kioskModeEnabled = kioskModeEnabled; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
