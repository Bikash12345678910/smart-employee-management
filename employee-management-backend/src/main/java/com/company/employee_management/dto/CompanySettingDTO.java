package com.company.employee_management.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public class CompanySettingDTO {

    private Long id;
    private String companyName;
    private String companyEmail;
    private String contactNumber;
    private String companyAddress;
    private String companyLogoUrl;

    // Shift
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private Integer lateArrivalGraceMinutes;
    private Integer halfDayThresholdHours;
    private Integer fullDayWorkingHours;
    private String weeklyOffDays;

    // Attendance
    private Boolean enableFaceAttendance;
    private Boolean enableFingerprintAttendance;
    private Boolean enablePasswordAttendance;
    private Boolean allowManualAttendance;
    private Integer maxBreaksAllowed;
    private Integer maxBreakMinutes;

    // Salary
    private String currencyCode = "INR";
    private String currencySymbol = "₹";
    private Integer standardWorkingDays;
    private String leaveDeductionRate;
    private BigDecimal overtimeRatePerHour;

    public CompanySettingDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    public String getCompanyLogoUrl() { return companyLogoUrl; }
    public void setCompanyLogoUrl(String companyLogoUrl) { this.companyLogoUrl = companyLogoUrl; }

    public LocalTime getShiftStartTime() { return shiftStartTime; }
    public void setShiftStartTime(LocalTime shiftStartTime) { this.shiftStartTime = shiftStartTime; }

    public LocalTime getShiftEndTime() { return shiftEndTime; }
    public void setShiftEndTime(LocalTime shiftEndTime) { this.shiftEndTime = shiftEndTime; }

    public Integer getLateArrivalGraceMinutes() { return lateArrivalGraceMinutes; }
    public void setLateArrivalGraceMinutes(Integer lateArrivalGraceMinutes) { this.lateArrivalGraceMinutes = lateArrivalGraceMinutes; }

    public Integer getHalfDayThresholdHours() { return halfDayThresholdHours; }
    public void setHalfDayThresholdHours(Integer halfDayThresholdHours) { this.halfDayThresholdHours = halfDayThresholdHours; }

    public Integer getFullDayWorkingHours() { return fullDayWorkingHours; }
    public void setFullDayWorkingHours(Integer fullDayWorkingHours) { this.fullDayWorkingHours = fullDayWorkingHours; }

    public String getWeeklyOffDays() { return weeklyOffDays; }
    public void setWeeklyOffDays(String weeklyOffDays) { this.weeklyOffDays = weeklyOffDays; }

    public Boolean getEnableFaceAttendance() { return enableFaceAttendance; }
    public void setEnableFaceAttendance(Boolean enableFaceAttendance) { this.enableFaceAttendance = enableFaceAttendance; }

    public Boolean getEnableFingerprintAttendance() { return enableFingerprintAttendance; }
    public void setEnableFingerprintAttendance(Boolean enableFingerprintAttendance) { this.enableFingerprintAttendance = enableFingerprintAttendance; }

    public Boolean getEnablePasswordAttendance() { return enablePasswordAttendance; }
    public void setEnablePasswordAttendance(Boolean enablePasswordAttendance) { this.enablePasswordAttendance = enablePasswordAttendance; }

    public Boolean getAllowManualAttendance() { return allowManualAttendance; }
    public void setAllowManualAttendance(Boolean allowManualAttendance) { this.allowManualAttendance = allowManualAttendance; }

    public Integer getMaxBreaksAllowed() { return maxBreaksAllowed; }
    public void setMaxBreaksAllowed(Integer maxBreaksAllowed) { this.maxBreaksAllowed = maxBreaksAllowed; }

    public Integer getMaxBreakMinutes() { return maxBreakMinutes; }
    public void setMaxBreakMinutes(Integer maxBreakMinutes) { this.maxBreakMinutes = maxBreakMinutes; }

    public String getCurrencyCode() { return currencyCode != null ? currencyCode : "INR"; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getCurrencySymbol() { return currencySymbol != null ? currencySymbol : "₹"; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public Integer getStandardWorkingDays() { return standardWorkingDays; }
    public void setStandardWorkingDays(Integer standardWorkingDays) { this.standardWorkingDays = standardWorkingDays; }

    public String getLeaveDeductionRate() { return leaveDeductionRate; }
    public void setLeaveDeductionRate(String leaveDeductionRate) { this.leaveDeductionRate = leaveDeductionRate; }

    public BigDecimal getOvertimeRatePerHour() { return overtimeRatePerHour; }
    public void setOvertimeRatePerHour(BigDecimal overtimeRatePerHour) { this.overtimeRatePerHour = overtimeRatePerHour; }
}
