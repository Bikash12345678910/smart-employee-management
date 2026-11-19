package com.company.employee_management.dto;

public class TerminalConfigDTO {
    private String terminalCode;
    private String terminalName;
    private String deviceModel;
    private String companyName;
    private Integer tapTimeoutMs;
    private Double faceMatchThreshold;
    private Boolean livenessRequired;
    private Boolean biometricPromptEnabled;
    private Boolean kioskModeEnabled;
    private Long serverTimeEpochMs;

    public TerminalConfigDTO() {}

    public String getTerminalCode() { return terminalCode; }
    public void setTerminalCode(String terminalCode) { this.terminalCode = terminalCode; }

    public String getTerminalName() { return terminalName; }
    public void setTerminalName(String terminalName) { this.terminalName = terminalName; }

    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Integer getTapTimeoutMs() { return tapTimeoutMs; }
    public void setTapTimeoutMs(Integer tapTimeoutMs) { this.tapTimeoutMs = tapTimeoutMs; }

    public Double getFaceMatchThreshold() { return faceMatchThreshold; }
    public void setFaceMatchThreshold(Double faceMatchThreshold) { this.faceMatchThreshold = faceMatchThreshold; }

    public Boolean getLivenessRequired() { return livenessRequired; }
    public void setLivenessRequired(Boolean livenessRequired) { this.livenessRequired = livenessRequired; }

    public Boolean getBiometricPromptEnabled() { return biometricPromptEnabled; }
    public void setBiometricPromptEnabled(Boolean biometricPromptEnabled) { this.biometricPromptEnabled = biometricPromptEnabled; }

    public Boolean getKioskModeEnabled() { return kioskModeEnabled; }
    public void setKioskModeEnabled(Boolean kioskModeEnabled) { this.kioskModeEnabled = kioskModeEnabled; }

    public Long getServerTimeEpochMs() { return serverTimeEpochMs; }
    public void setServerTimeEpochMs(Long serverTimeEpochMs) { this.serverTimeEpochMs = serverTimeEpochMs; }
}
