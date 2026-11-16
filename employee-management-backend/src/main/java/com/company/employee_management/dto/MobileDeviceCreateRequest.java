package com.company.employee_management.dto;

import jakarta.validation.constraints.NotBlank;

public class MobileDeviceCreateRequest {

    @NotBlank(message = "Device name is required")
    private String deviceName;

    private String deviceModel = "Galaxy Tab A9+";

    private String deviceSerialNumber;

    private String ipAddress = "192.168.1.100";

    private Integer port = 4370;

    private String location = "Main Reception Desk";

    private String manufacturer = "Samsung";

    private String androidVersion = "Android 14";

    private String appVersion = "v2.4.0-kiosk";

    private String departmentName = "General Staff";

    private String connectionType = "Wi-Fi"; // Wi-Fi, Ethernet / LAN, Mobile Data

    private String wifiStatus = "Connected (5GHz, -50dBm)";

    private String status = "ONLINE"; // ONLINE, OFFLINE, MAINTENANCE, CONNECTED, DISCONNECTED, SYNCHRONIZING, ERROR, INACTIVE

    public MobileDeviceCreateRequest() {}

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
