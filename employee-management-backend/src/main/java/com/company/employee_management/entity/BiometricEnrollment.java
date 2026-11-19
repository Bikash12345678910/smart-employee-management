package com.company.employee_management.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "biometric_enrollments")
public class BiometricEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "employee_code", nullable = false, length = 50)
    private String employeeCode;

    @Column(name = "modality", nullable = false, length = 30)
    private String modality = "FACE_CAMERAX";

    @Column(name = "encrypted_template", nullable = false, columnDefinition = "TEXT")
    private String encryptedTemplate;

    @Column(name = "template_version", nullable = false, length = 20)
    private String templateVersion = "MLKIT_V1_28D";

    @Column(name = "quality_score", precision = 5, scale = 4)
    private BigDecimal qualityScore = new BigDecimal("0.9000");

    @Column(name = "consent_given", nullable = false)
    private Boolean consentGiven = false;

    @Column(name = "consent_text_version", nullable = false, length = 30)
    private String consentTextVersion = "CONSENT_V1.0";

    @Column(name = "consent_timestamp", nullable = false)
    private LocalDateTime consentTimestamp = LocalDateTime.now();

    @Column(name = "enrolled_by_admin", nullable = false, length = 100)
    private String enrolledByAdmin = "admin";

    @Column(name = "terminal_code", length = 64)
    private String terminalCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public BiometricEnrollment() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }

    public String getEncryptedTemplate() { return encryptedTemplate; }
    public void setEncryptedTemplate(String encryptedTemplate) { this.encryptedTemplate = encryptedTemplate; }

    public String getTemplateVersion() { return templateVersion; }
    public void setTemplateVersion(String templateVersion) { this.templateVersion = templateVersion; }

    public BigDecimal getQualityScore() { return qualityScore; }
    public void setQualityScore(BigDecimal qualityScore) { this.qualityScore = qualityScore; }

    public Boolean getConsentGiven() { return consentGiven; }
    public void setConsentGiven(Boolean consentGiven) { this.consentGiven = consentGiven; }

    public String getConsentTextVersion() { return consentTextVersion; }
    public void setConsentTextVersion(String consentTextVersion) { this.consentTextVersion = consentTextVersion; }

    public LocalDateTime getConsentTimestamp() { return consentTimestamp; }
    public void setConsentTimestamp(LocalDateTime consentTimestamp) { this.consentTimestamp = consentTimestamp; }

    public String getEnrolledByAdmin() { return enrolledByAdmin; }
    public void setEnrolledByAdmin(String enrolledByAdmin) { this.enrolledByAdmin = enrolledByAdmin; }

    public String getTerminalCode() { return terminalCode; }
    public void setTerminalCode(String terminalCode) { this.terminalCode = terminalCode; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
