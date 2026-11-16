package com.company.employee_management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "biometric_registrations")
public class BiometricRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "biometric_type", nullable = false, length = 30)
    private String biometricType; // FACE, FINGERPRINT

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id")
    private BiometricDevice device;

    @JsonIgnore
    @Column(name = "template_hash", nullable = false)
    private String templateHash; // Secure one-way hashed biometric template

    @Column(name = "face_sample_data", columnDefinition = "TEXT")
    private String faceSampleData;

    @Column(name = "fingerprint_slot")
    private Integer fingerprintSlot;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    public BiometricRegistration() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getBiometricType() { return biometricType; }
    public void setBiometricType(String biometricType) { this.biometricType = biometricType; }

    public BiometricDevice getDevice() { return device; }
    public void setDevice(BiometricDevice device) { this.device = device; }

    public String getTemplateHash() { return templateHash; }
    public void setTemplateHash(String templateHash) { this.templateHash = templateHash; }

    public String getFaceSampleData() { return faceSampleData; }
    public void setFaceSampleData(String faceSampleData) { this.faceSampleData = faceSampleData; }

    public Integer getFingerprintSlot() { return fingerprintSlot; }
    public void setFingerprintSlot(Integer fingerprintSlot) { this.fingerprintSlot = fingerprintSlot; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
}
