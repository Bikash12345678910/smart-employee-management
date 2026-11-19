package com.company.employee_management.dto;

public class TerminalPunchRequest {
    private String employeeCode;
    private String action; // CHECK_IN or CHECK_OUT
    private double[] faceVector;
    private Boolean livenessPassed;
    private Double livenessScore;
    private String livenessChallenge; // e.g., BLINK_DETECTED, HEAD_TURN
    private Boolean biometricPromptUsed;
    private String verificationMethod; // FACE_LIVENESS, FACE_AND_BIOMETRIC_PROMPT
    private String nonce;
    private Long clientTimestampMs;

    public TerminalPunchRequest() {}

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public double[] getFaceVector() { return faceVector; }
    public void setFaceVector(double[] faceVector) { this.faceVector = faceVector; }

    public Boolean getLivenessPassed() { return livenessPassed; }
    public void setLivenessPassed(Boolean livenessPassed) { this.livenessPassed = livenessPassed; }

    public Double getLivenessScore() { return livenessScore; }
    public void setLivenessScore(Double livenessScore) { this.livenessScore = livenessScore; }

    public String getLivenessChallenge() { return livenessChallenge; }
    public void setLivenessChallenge(String livenessChallenge) { this.livenessChallenge = livenessChallenge; }

    public Boolean getBiometricPromptUsed() { return biometricPromptUsed; }
    public void setBiometricPromptUsed(Boolean biometricPromptUsed) { this.biometricPromptUsed = biometricPromptUsed; }

    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public Long getClientTimestampMs() { return clientTimestampMs; }
    public void setClientTimestampMs(Long clientTimestampMs) { this.clientTimestampMs = clientTimestampMs; }
}
