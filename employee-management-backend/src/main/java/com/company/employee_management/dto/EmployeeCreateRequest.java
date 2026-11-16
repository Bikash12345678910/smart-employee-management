package com.company.employee_management.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeCreateRequest {

    // Personal Info
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String profilePhotoUrl;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    // Verifications
    private String phoneOtp;
    private String emailOtp;

    // Employment
    private String employeeCode; // If null, auto-generated

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private Long departmentId;

    private Long designationId;

    private String employmentStatus = "ACTIVE";

    @NotNull(message = "Agreed monthly salary is required")
    @Positive(message = "Salary must be positive")
    private BigDecimal agreedMonthlySalary;

    // Financial
    @NotBlank(message = "Aadhaar number is required")
    private String aadhaarNumber;

    @NotBlank(message = "PAN number is required")
    private String panNumber;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "IFSC code is required")
    private String bankIfscCode;

    private String bankName;

    // Biometrics
    private String liveFaceData; // Base64 or hash preview
    private Boolean registerFace = false;
    private Boolean registerFingerprint = false;
    private Long fingerprintDeviceId;

    // Login credentials
    @NotBlank(message = "Employee login username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String confirmPassword;

    public EmployeeCreateRequest() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneOtp() { return phoneOtp; }
    public void setPhoneOtp(String phoneOtp) { this.phoneOtp = phoneOtp; }

    public String getEmailOtp() { return emailOtp; }
    public void setEmailOtp(String emailOtp) { this.emailOtp = emailOtp; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getDesignationId() { return designationId; }
    public void setDesignationId(Long designationId) { this.designationId = designationId; }

    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }

    public BigDecimal getAgreedMonthlySalary() { return agreedMonthlySalary; }
    public void setAgreedMonthlySalary(BigDecimal agreedMonthlySalary) { this.agreedMonthlySalary = agreedMonthlySalary; }

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String bankIfscCode) { this.bankIfscCode = bankIfscCode; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getLiveFaceData() { return liveFaceData; }
    public void setLiveFaceData(String liveFaceData) { this.liveFaceData = liveFaceData; }

    public Boolean getRegisterFace() { return registerFace; }
    public void setRegisterFace(Boolean registerFace) { this.registerFace = registerFace; }

    public Boolean getRegisterFingerprint() { return registerFingerprint; }
    public void setRegisterFingerprint(Boolean registerFingerprint) { this.registerFingerprint = registerFingerprint; }

    public Long getFingerprintDeviceId() { return fingerprintDeviceId; }
    public void setFingerprintDeviceId(Long fingerprintDeviceId) { this.fingerprintDeviceId = fingerprintDeviceId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
