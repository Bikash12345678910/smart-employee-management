package com.company.employee_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_activity_logs")
public class DeviceActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "device_uuid", length = 100)
    private String deviceUuid;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType; // CONNECTED, DISCONNECTED, SYNC, ERROR, KIOSK_LOCK, PING

    @Column(nullable = false, length = 20)
    private String severity = "INFO"; // INFO, WARNING, ERROR, SUCCESS

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DeviceActivityLog() {}

    public DeviceActivityLog(Long deviceId, String deviceUuid, String deviceName, String activityType, String severity, String details) {
        this.deviceId = deviceId;
        this.deviceUuid = deviceUuid;
        this.deviceName = deviceName;
        this.activityType = activityType;
        this.severity = severity;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public String getDeviceUuid() { return deviceUuid; }
    public void setDeviceUuid(String deviceUuid) { this.deviceUuid = deviceUuid; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
