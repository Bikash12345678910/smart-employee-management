package com.company.employee_management.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeDTO {

    private Long id;
    private String employeeCode;
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String profilePhotoUrl;

    // Employment
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Long designationId;
    private String designationTitle;
    private LocalDate joiningDate;
    private String employmentStatus;
    private BigDecimal agreedMonthlySalary;

    // Financial Masked
    private String aadhaarMasked;
    private String panMasked;
    private String bankAccountMasked;
    private String bankIfscCode;
    private String bankName;

    // Raw (only accessible to admin if needed)
    private String aadhaarNumber;
    private String panNumber;
    private String bankAccountNumber;

    // Biometrics & Status
    private Boolean isFaceRegistered = false;
    private Boolean isFingerprintRegistered = false;
    private Boolean isPhoneVerified = false;
    private Boolean isEmailVerified = false;
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeDTO() {}

    public static String maskAadhaar(String raw) {
        if (raw == null || raw.length() < 4) return "•••• •••• ••••";
        String clean = raw.replaceAll("[^0-9]", "");
        if (clean.length() >= 4) {
            String last4 = clean.substring(clean.length() - 4);
            return "•••• •••• " + last4;
        }
        return "•••• •••• ••••";
    }

    public static String maskPan(String raw) {
        if (raw == null || raw.length() < 4) return "••••• •••• •";
        if (raw.length() >= 5) {
            return raw.substring(0, 2) + "•••" + raw.substring(raw.length() - 2);
        }
        return "•••••" + raw;
    }

    public static String maskBankAccount(String raw) {
        if (raw == null || raw.length() < 4) return "••••••••••••";
        String clean = raw.trim();
        String last4 = clean.substring(clean.length() - 4);
        return "••••••••" + last4;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public Long getDesignationId() { return designationId; }
    public void setDesignationId(Long designationId) { this.designationId = designationId; }

    public String getDesignationTitle() { return designationTitle; }
    public void setDesignationTitle(String designationTitle) { this.designationTitle = designationTitle; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }

    public BigDecimal getAgreedMonthlySalary() { return agreedMonthlySalary; }
    public void setAgreedMonthlySalary(BigDecimal agreedMonthlySalary) { this.agreedMonthlySalary = agreedMonthlySalary; }

    public String getAadhaarMasked() { return aadhaarMasked; }
    public void setAadhaarMasked(String aadhaarMasked) { this.aadhaarMasked = aadhaarMasked; }

    public String getPanMasked() { return panMasked; }
    public void setPanMasked(String panMasked) { this.panMasked = panMasked; }

    public String getBankAccountMasked() { return bankAccountMasked; }
    public void setBankAccountMasked(String bankAccountMasked) { this.bankAccountMasked = bankAccountMasked; }

    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String bankIfscCode) { this.bankIfscCode = bankIfscCode; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

    public Boolean getIsFaceRegistered() { return isFaceRegistered; }
    public void setIsFaceRegistered(Boolean isFaceRegistered) { this.isFaceRegistered = isFaceRegistered; }

    public Boolean getIsFingerprintRegistered() { return isFingerprintRegistered; }
    public void setIsFingerprintRegistered(Boolean isFingerprintRegistered) { this.isFingerprintRegistered = isFingerprintRegistered; }

    public Boolean getIsPhoneVerified() { return isPhoneVerified; }
    public void setIsPhoneVerified(Boolean isPhoneVerified) { this.isPhoneVerified = isPhoneVerified; }

    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
