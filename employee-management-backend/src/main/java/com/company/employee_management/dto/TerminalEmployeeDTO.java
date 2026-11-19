package com.company.employee_management.dto;

public class TerminalEmployeeDTO {
    private Long id;
    private String employeeCode;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Boolean isActive;
    private Boolean faceEnrolled;
    private Boolean consentGiven;
    private String todayStatus; // NOT_CHECKED_IN, CHECKED_IN, CHECKED_OUT
    private String checkInTime;
    private String checkOutTime;

    public TerminalEmployeeDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getFaceEnrolled() { return faceEnrolled; }
    public void setFaceEnrolled(Boolean faceEnrolled) { this.faceEnrolled = faceEnrolled; }

    public Boolean getConsentGiven() { return consentGiven; }
    public void setConsentGiven(Boolean consentGiven) { this.consentGiven = consentGiven; }

    public String getTodayStatus() { return todayStatus; }
    public void setTodayStatus(String todayStatus) { this.todayStatus = todayStatus; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }
}
