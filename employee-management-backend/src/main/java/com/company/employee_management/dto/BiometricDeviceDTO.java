package com.company.employee_management.dto;

import java.time.LocalDateTime;

public class BiometricDeviceDTO {

    private Long id;
    private String deviceName;
    private String deviceModel;
    private String deviceSerialNumber;
    private String ipAddress;
    private Integer port;
    private String location;
    private String status; // ONLINE, OFFLINE, MAINTENANCE, CONNECTED, DISCONNECTED, SYNCHRONIZING, ERROR, INACTIVE
    private String deviceUuid;
    private String manufacturer;
    private String androidVersion;
    private String appVersion;
    private String departmentName;
    private String connectionType;
    private String wifiStatus;
    private String deviceType = "BIOMETRIC"; // MOBILE, BIOMETRIC
    private String devicePlatform = "Android";
    private Long assignedEmployeeId;
    private String assignedEmployeeName;
    private String communicationProtocol = "TCP/IP";
    private String deviceUsername;
    private String devicePassword;
    private LocalDateTime lastSyncTime;
    private LocalDateTime createdAt;

    public BiometricDeviceDTO() {}

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
