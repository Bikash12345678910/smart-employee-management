package com.company.employee_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_identifier", nullable = false, length = 100)
    private String targetIdentifier; // Phone number or email

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "otp_type", nullable = false, length = 20)
    private String otpType; // PHONE, EMAIL

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public OtpVerification() {}

    public OtpVerification(String targetIdentifier, String otpCode, String otpType, LocalDateTime expiresAt) {
        this.targetIdentifier = targetIdentifier;
        this.otpCode = otpCode;
        this.otpType = otpType;
        this.expiresAt = expiresAt;
        this.isVerified = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTargetIdentifier() { return targetIdentifier; }
    public void setTargetIdentifier(String targetIdentifier) { this.targetIdentifier = targetIdentifier; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public String getOtpType() { return otpType; }
    public void setOtpType(String otpType) { this.otpType = otpType; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
