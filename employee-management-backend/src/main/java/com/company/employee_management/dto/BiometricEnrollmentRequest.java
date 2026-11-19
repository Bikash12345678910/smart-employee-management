package com.company.employee_management.dto;

public class BiometricEnrollmentRequest {
    private String employeeCode;
    private double[] faceVector;
    private Boolean consentGiven;
    private String consentTextVersion;
    private Double qualityScore;
    private String enrolledByAdmin;

    public BiometricEnrollmentRequest() {}

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public double[] getFaceVector() { return faceVector; }
    public void setFaceVector(double[] faceVector) { this.faceVector = faceVector; }

    public Boolean getConsentGiven() { return consentGiven; }
    public void setConsentGiven(Boolean consentGiven) { this.consentGiven = consentGiven; }

    public String getConsentTextVersion() { return consentTextVersion; }
    public void setConsentTextVersion(String consentTextVersion) { this.consentTextVersion = consentTextVersion; }

    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }

    public String getEnrolledByAdmin() { return enrolledByAdmin; }
    public void setEnrolledByAdmin(String enrolledByAdmin) { this.enrolledByAdmin = enrolledByAdmin; }
}
