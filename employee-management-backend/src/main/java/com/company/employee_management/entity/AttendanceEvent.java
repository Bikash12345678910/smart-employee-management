package com.company.employee_management.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime = LocalDateTime.now();

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType; // CHECK_IN, CHECK_OUT

    @Column(name = "punch_method", nullable = false, length = 30)
    private String punchMethod; // FACE, FINGERPRINT, PASSWORD, ADMIN_MANUAL

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id")
    private BiometricDevice device;

    @Column(name = "verification_score", precision = 5, scale = 2)
    private BigDecimal verificationScore = new BigDecimal("100.00");

    @Column(name = "device_log_id", length = 100)
    private String deviceLogId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AttendanceEvent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPunchMethod() { return punchMethod; }
    public void setPunchMethod(String punchMethod) { this.punchMethod = punchMethod; }

    public BiometricDevice getDevice() { return device; }
    public void setDevice(BiometricDevice device) { this.device = device; }

    public BigDecimal getVerificationScore() { return verificationScore; }
    public void setVerificationScore(BigDecimal verificationScore) { this.verificationScore = verificationScore; }

    public String getDeviceLogId() { return deviceLogId; }
    public void setDeviceLogId(String deviceLogId) { this.deviceLogId = deviceLogId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
