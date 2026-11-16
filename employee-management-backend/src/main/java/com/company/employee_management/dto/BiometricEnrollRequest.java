package com.company.employee_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BiometricEnrollRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotBlank(message = "Biometric type is required (FACE or FINGERPRINT)")
    private String biometricType; // FACE, FINGERPRINT

    private Long deviceId;
    private String rawSampleOrTemplate;
    private Integer slotNumber;

    public BiometricEnrollRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getBiometricType() { return biometricType; }
    public void setBiometricType(String biometricType) { this.biometricType = biometricType; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public String getRawSampleOrTemplate() { return rawSampleOrTemplate; }
    public void setRawSampleOrTemplate(String rawSampleOrTemplate) { this.rawSampleOrTemplate = rawSampleOrTemplate; }

    public Integer getSlotNumber() { return slotNumber; }
    public void setSlotNumber(Integer slotNumber) { this.slotNumber = slotNumber; }
}
