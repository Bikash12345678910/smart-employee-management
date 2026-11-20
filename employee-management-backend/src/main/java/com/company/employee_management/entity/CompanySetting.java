package com.company.employee_management.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "company_settings")
public class CompanySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName = "Apex Global Technologies Inc.";

    @Column(name = "company_email", nullable = false, length = 150)
    private String companyEmail = "admin@apextech.com";

    @Column(name = "contact_number", nullable = false, length = 30)
    private String contactNumber = "+1 (800) 555-0199";

    @Column(name = "company_address", columnDefinition = "TEXT", nullable = false)
    private String companyAddress = "100 Tech Park Boulevard, Suite 500, Silicon City, CA";

    @Column(name = "company_logo_url", columnDefinition = "LONGTEXT")
    private String companyLogoUrl;

    // Shift Settings
    @Column(name = "shift_start_time", nullable = false)
    private LocalTime shiftStartTime = LocalTime.of(9, 0, 0);

    @Column(name = "shift_end_time", nullable = false)
    private LocalTime shiftEndTime = LocalTime.of(18, 0, 0);

    @Column(name = "late_arrival_grace_minutes", nullable = false)
    private Integer lateArrivalGraceMinutes = 15;

    @Column(name = "half_day_threshold_hours", nullable = false)
    private Integer halfDayThresholdHours = 4;

    @Column(name = "full_day_working_hours", nullable = false)
    private Integer fullDayWorkingHours = 8;

    @Column(name = "weekly_off_days", length = 100)
    private String weeklyOffDays = "SUNDAY";

    // Attendance Policy
    @Column(name = "enable_face_attendance")
    private Boolean enableFaceAttendance = true;

    @Column(name = "enable_fingerprint_attendance")
    private Boolean enableFingerprintAttendance = true;

    @Column(name = "enable_password_attendance")
    private Boolean enablePasswordAttendance = true;

    @Column(name = "allow_manual_attendance")
    private Boolean allowManualAttendance = true;

    @Column(name = "max_breaks_allowed")
    private Integer maxBreaksAllowed = 3;

    @Column(name = "max_break_minutes")
    private Integer maxBreakMinutes = 60;

    // Salary Policy
    @Column(name = "currency_code", length = 10)
    private String currencyCode = "INR";

    @Column(name = "currency_symbol", length = 10)
    private String currencySymbol = "₹";

    @Column(name = "standard_working_days")
    private Integer standardWorkingDays = 26;

    @Column(name = "leave_deduction_rate", length = 20)
    private String leaveDeductionRate = "PRO_RATA"; // PRO_RATA, FIXED_DAILY

    @Column(name = "overtime_rate_per_hour", precision = 8, scale = 2)
    private BigDecimal overtimeRatePerHour = new BigDecimal("200.00");

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public CompanySetting() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

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

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
