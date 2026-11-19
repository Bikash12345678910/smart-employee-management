package com.company.employee_management.dto;

public class TerminalPunchResponse {
    private boolean verified;
    private String action;
    private String employeeCode;
    private String employeeName;
    private String attendanceDate;
    private String serverTimestamp;
    private String checkInTime;
    private String checkOutTime;
    private Long workingMinutes;
    private Double matchScore;
    private String verificationStatus;
    private String message;

    public TerminalPunchResponse() {}

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(String attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getServerTimestamp() { return serverTimestamp; }
    public void setServerTimestamp(String serverTimestamp) { this.serverTimestamp = serverTimestamp; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

    public Long getWorkingMinutes() { return workingMinutes; }
    public void setWorkingMinutes(Long workingMinutes) { this.workingMinutes = workingMinutes; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
