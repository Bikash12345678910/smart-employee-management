package com.company.employee_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "biometric_devices")
public class BiometricDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "device_model", nullable = false, length = 50)
    private String deviceModel;

    @Column(name = "device_serial_number", nullable = false, unique = true, length = 100)
    private String deviceSerialNumber;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private Integer port = 4370;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(length = 20)
    private String status = "ONLINE"; // ONLINE, OFFLINE, MAINTENANCE, CONNECTED, DISCONNECTED, SYNCHRONIZING, ERROR, INACTIVE

    @Column(name = "device_uuid", length = 100)
    private String deviceUuid;

    @Column(length = 100)
    private String manufacturer = "Samsung";

    @Column(name = "android_version", length = 50)
    private String androidVersion = "Android 14";

    @Column(name = "app_version", length = 50)
    private String appVersion = "v2.4.0-kiosk";

    @Column(name = "department_name", length = 100)
    private String departmentName = "Engineering";

    @Column(name = "connection_type", length = 50)
    private String connectionType = "Wi-Fi"; // Wi-Fi, Ethernet / LAN, Mobile Data

    @Column(name = "device_type", length = 20)
    private String deviceType = "BIOMETRIC"; // MOBILE, BIOMETRIC

    @Column(name = "device_platform", length = 50)
    private String devicePlatform = "Android";

    @Column(name = "assigned_employee_id")
    private Long assignedEmployeeId;

    @Column(name = "assigned_employee_name", length = 100)
    private String assignedEmployeeName;

    @Column(name = "communication_protocol", length = 50)
    private String communicationProtocol = "TCP/IP";

    @Column(name = "device_username", length = 100)
    private String deviceUsername;

    @Column(name = "device_password")
    private String devicePassword;

    @Column(name = "wifi_status", length = 50)
    private String wifiStatus = "Connected (5GHz, -52dBm)";

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public BiometricDevice() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

    public String getDeviceSerialNumber() { return deviceSerialNumber; }
    public void setDeviceSerialNumber(String deviceSerialNumber) { this.deviceSerialNumber = deviceSerialNumber; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDeviceUuid() { return deviceUuid; }
    public void setDeviceUuid(String deviceUuid) { this.deviceUuid = deviceUuid; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getAndroidVersion() { return androidVersion; }
    public void setAndroidVersion(String androidVersion) { this.androidVersion = androidVersion; }

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getConnectionType() { return connectionType; }
    public void setConnectionType(String connectionType) { this.connectionType = connectionType; }

    public String getWifiStatus() { return wifiStatus; }
    public void setWifiStatus(String wifiStatus) { this.wifiStatus = wifiStatus; }

    public String getDeviceType() { return deviceType != null ? deviceType : "BIOMETRIC"; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getDevicePlatform() { return devicePlatform; }
    public void setDevicePlatform(String devicePlatform) { this.devicePlatform = devicePlatform; }

    public Long getAssignedEmployeeId() { return assignedEmployeeId; }
    public void setAssignedEmployeeId(Long assignedEmployeeId) { this.assignedEmployeeId = assignedEmployeeId; }

    public String getAssignedEmployeeName() { return assignedEmployeeName; }
    public void setAssignedEmployeeName(String assignedEmployeeName) { this.assignedEmployeeName = assignedEmployeeName; }

    public String getCommunicationProtocol() { return communicationProtocol; }
    public void setCommunicationProtocol(String communicationProtocol) { this.communicationProtocol = communicationProtocol; }

    public String getDeviceUsername() { return deviceUsername; }
    public void setDeviceUsername(String deviceUsername) { this.deviceUsername = deviceUsername; }

    public String getDevicePassword() { return devicePassword; }
    public void setDevicePassword(String devicePassword) { this.devicePassword = devicePassword; }

    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
