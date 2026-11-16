package com.company.employee_management.dto;

import jakarta.validation.constraints.NotBlank;

public class AttendancePunchRequest {

    private Long employeeId;
    private String employeeCode;

    @NotBlank(message = "Punch type is required (CHECK_IN or CHECK_OUT)")
    private String punchType; // CHECK_IN, CHECK_OUT

    @NotBlank(message = "Punch method is required")
    private String punchMethod; // FACE, FINGERPRINT, PASSWORD, MANUAL

    private String biometricData; // Hashed template or camera capture embedding
    private String password; // If punching via password
    private Long deviceId;

    public AttendancePunchRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getPunchType() { return punchType; }
    public void setPunchType(String punchType) { this.punchType = punchType; }

    public String getPunchMethod() { return punchMethod; }
    public void setPunchMethod(String punchMethod) { this.punchMethod = punchMethod; }

    public String getBiometricData() { return biometricData; }
    public void setBiometricData(String biometricData) { this.biometricData = biometricData; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
}
