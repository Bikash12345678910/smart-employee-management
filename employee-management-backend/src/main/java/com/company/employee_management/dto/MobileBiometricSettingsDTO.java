package com.company.employee_management.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MobileBiometricSettingsDTO {

    private Long id;

    // 1. Camera Configuration
    private Boolean cameraEnabled = true;
    private Boolean frontCameraSelected = true;
    private Boolean faceDetectionEnabled = true;
    private Integer minFaceQuality = 85;
    private Boolean brightnessCheckEnabled = true;
    private Boolean positionGuidanceEnabled = true;
    private Boolean multipleFaceDetectionPrevented = true;

    // 2. Face Biometric Configuration
    private Boolean faceVerificationEnabled = true;
    private Integer matchingThreshold = 80;
    private Boolean livenessDetectionEnabled = true;
    private Boolean mismatchAlertEnabled = true;
    private Integer maxFailedAttempts = 3;
    private Integer verificationRetrySeconds = 30;
    private Boolean requireEmployeeConsent = true;

    // 3. GPS & Location Configuration
    private Boolean gpsVerificationEnabled = true;
    private String officeName = "Apex Global Tech HQ";
    private String officeAddress = "100 Tech Park Boulevard, Suite 500, Silicon City";
    private BigDecimal officeLatitude = new BigDecimal("37.7749290");
    private BigDecimal officeLongitude = new BigDecimal("-122.4194160");
    private Integer permittedRadiusMeters = 150;
    private Integer gpsAccuracyRequirementMeters = 25;

    // 4. Attendance Policy
    private Boolean mobileCheckinEnabled = true;
    private Boolean mobileCheckoutEnabled = true;
    private String allowedCheckinStart = "07:30";
    private String allowedCheckinEnd = "11:00";
    private String allowedCheckoutStart = "17:00";
    private String allowedCheckoutEnd = "22:00";
    private Integer attendanceGracePeriodMins = 15;
    private Boolean calculateLateArrival = true;
    private Boolean calculateEarlyDeparture = true;
    private Boolean calculateOvertime = true;
    private Integer minWorkingHoursPerDay = 8;
    private Integer preventDuplicatePunchMins = 5;
    private Boolean requireServerTimeValidation = true;

    // 5. Kiosk Security Configuration
    private Boolean kioskModeEnabled = true;
    private Boolean preventScreenLock = true;
    private Boolean appAutoLaunch = true;
    private Boolean deviceAuthRequired = true;
    private String secureDeviceToken = "APEX-SEC-DEVICE-TOKEN-9843759247";
    private Integer deviceTokenExpirationDays = 90;
    private Integer sessionTimeoutMins = 10;
    private Integer maxFailedKioskAttempts = 5;
    private Integer tempLockoutDurationMins = 15;
    private Boolean requireHttps = true;

    // 6. QR Code Configuration
    private Boolean qrScannerEnabled = true;
    private Integer qrExpirationMins = 1440;
    private Boolean qrRegenerationAllowed = true;
    private String qrSigningSecret = "APEX-QR-SIGNING-SECRET-KEY-9988";

    // 7. Notification Configuration
    private Boolean notifySuccessCheckin = true;
    private Boolean notifySuccessCheckout = true;
    private Boolean notifyFailedFace = true;
    private Boolean notifyGpsFailure = true;
    private Boolean notifyDeviceDisconnected = true;
    private Boolean notifyCameraError = true;
    private Boolean notifyRepeatedFailures = true;
    private String adminAlertEmail = "admin@apextech.com";

    // 8. Synchronization Configuration
    private Boolean autoSyncEnabled = true;
    private Integer syncRetryIntervalSecs = 60;
    private Boolean offlinePunchStorageEnabled = true;

    // 9. Privacy & Data Management
    private Integer biometricRetentionDays = 365;
    private Boolean encryptBiometricTemplates = true;
    private Boolean enableAuditLogging = true;

    private LocalDateTime updatedAt;
    private String updatedBy;

    public MobileBiometricSettingsDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Boolean getCameraEnabled() { return cameraEnabled; }
    public void setCameraEnabled(Boolean cameraEnabled) { this.cameraEnabled = cameraEnabled; }

    public Boolean getFrontCameraSelected() { return frontCameraSelected; }
    public void setFrontCameraSelected(Boolean frontCameraSelected) { this.frontCameraSelected = frontCameraSelected; }

    public Boolean getFaceDetectionEnabled() { return faceDetectionEnabled; }
    public void setFaceDetectionEnabled(Boolean faceDetectionEnabled) { this.faceDetectionEnabled = faceDetectionEnabled; }

    public Integer getMinFaceQuality() { return minFaceQuality; }
    public void setMinFaceQuality(Integer minFaceQuality) { this.minFaceQuality = minFaceQuality; }

    public Boolean getBrightnessCheckEnabled() { return brightnessCheckEnabled; }
    public void setBrightnessCheckEnabled(Boolean brightnessCheckEnabled) { this.brightnessCheckEnabled = brightnessCheckEnabled; }

    public Boolean getPositionGuidanceEnabled() { return positionGuidanceEnabled; }
    public void setPositionGuidanceEnabled(Boolean positionGuidanceEnabled) { this.positionGuidanceEnabled = positionGuidanceEnabled; }

    public Boolean getMultipleFaceDetectionPrevented() { return multipleFaceDetectionPrevented; }
    public void setMultipleFaceDetectionPrevented(Boolean multipleFaceDetectionPrevented) { this.multipleFaceDetectionPrevented = multipleFaceDetectionPrevented; }

    public Boolean getFaceVerificationEnabled() { return faceVerificationEnabled; }
    public void setFaceVerificationEnabled(Boolean faceVerificationEnabled) { this.faceVerificationEnabled = faceVerificationEnabled; }

    public Integer getMatchingThreshold() { return matchingThreshold; }
    public void setMatchingThreshold(Integer matchingThreshold) { this.matchingThreshold = matchingThreshold; }

    public Boolean getLivenessDetectionEnabled() { return livenessDetectionEnabled; }
    public void setLivenessDetectionEnabled(Boolean livenessDetectionEnabled) { this.livenessDetectionEnabled = livenessDetectionEnabled; }

    public Boolean getMismatchAlertEnabled() { return mismatchAlertEnabled; }
    public void setMismatchAlertEnabled(Boolean mismatchAlertEnabled) { this.mismatchAlertEnabled = mismatchAlertEnabled; }

    public Integer getMaxFailedAttempts() { return maxFailedAttempts; }
    public void setMaxFailedAttempts(Integer maxFailedAttempts) { this.maxFailedAttempts = maxFailedAttempts; }

    public Integer getVerificationRetrySeconds() { return verificationRetrySeconds; }
    public void setVerificationRetrySeconds(Integer verificationRetrySeconds) { this.verificationRetrySeconds = verificationRetrySeconds; }

    public Boolean getRequireEmployeeConsent() { return requireEmployeeConsent; }
    public void setRequireEmployeeConsent(Boolean requireEmployeeConsent) { this.requireEmployeeConsent = requireEmployeeConsent; }

    public Boolean getGpsVerificationEnabled() { return gpsVerificationEnabled; }
    public void setGpsVerificationEnabled(Boolean gpsVerificationEnabled) { this.gpsVerificationEnabled = gpsVerificationEnabled; }

    public String getOfficeName() { return officeName; }
    public void setOfficeName(String officeName) { this.officeName = officeName; }

    public String getOfficeAddress() { return officeAddress; }
    public void setOfficeAddress(String officeAddress) { this.officeAddress = officeAddress; }

    public BigDecimal getOfficeLatitude() { return officeLatitude; }
    public void setOfficeLatitude(BigDecimal officeLatitude) { this.officeLatitude = officeLatitude; }

    public BigDecimal getOfficeLongitude() { return officeLongitude; }
    public void setOfficeLongitude(BigDecimal officeLongitude) { this.officeLongitude = officeLongitude; }

    public Integer getPermittedRadiusMeters() { return permittedRadiusMeters; }
    public void setPermittedRadiusMeters(Integer permittedRadiusMeters) { this.permittedRadiusMeters = permittedRadiusMeters; }

    public Integer getGpsAccuracyRequirementMeters() { return gpsAccuracyRequirementMeters; }
    public void setGpsAccuracyRequirementMeters(Integer gpsAccuracyRequirementMeters) { this.gpsAccuracyRequirementMeters = gpsAccuracyRequirementMeters; }

    public Boolean getMobileCheckinEnabled() { return mobileCheckinEnabled; }
    public void setMobileCheckinEnabled(Boolean mobileCheckinEnabled) { this.mobileCheckinEnabled = mobileCheckinEnabled; }

    public Boolean getMobileCheckoutEnabled() { return mobileCheckoutEnabled; }
    public void setMobileCheckoutEnabled(Boolean mobileCheckoutEnabled) { this.mobileCheckoutEnabled = mobileCheckoutEnabled; }

    public String getAllowedCheckinStart() { return allowedCheckinStart; }
    public void setAllowedCheckinStart(String allowedCheckinStart) { this.allowedCheckinStart = allowedCheckinStart; }

    public String getAllowedCheckinEnd() { return allowedCheckinEnd; }
    public void setAllowedCheckinEnd(String allowedCheckinEnd) { this.allowedCheckinEnd = allowedCheckinEnd; }

    public String getAllowedCheckoutStart() { return allowedCheckoutStart; }
    public void setAllowedCheckoutStart(String allowedCheckoutStart) { this.allowedCheckoutStart = allowedCheckoutStart; }

    public String getAllowedCheckoutEnd() { return allowedCheckoutEnd; }
    public void setAllowedCheckoutEnd(String allowedCheckoutEnd) { this.allowedCheckoutEnd = allowedCheckoutEnd; }

    public Integer getAttendanceGracePeriodMins() { return attendanceGracePeriodMins; }
    public void setAttendanceGracePeriodMins(Integer attendanceGracePeriodMins) { this.attendanceGracePeriodMins = attendanceGracePeriodMins; }

    public Boolean getCalculateLateArrival() { return calculateLateArrival; }
    public void setCalculateLateArrival(Boolean calculateLateArrival) { this.calculateLateArrival = calculateLateArrival; }

    public Boolean getCalculateEarlyDeparture() { return calculateEarlyDeparture; }
    public void setCalculateEarlyDeparture(Boolean calculateEarlyDeparture) { this.calculateEarlyDeparture = calculateEarlyDeparture; }

    public Boolean getCalculateOvertime() { return calculateOvertime; }
    public void setCalculateOvertime(Boolean calculateOvertime) { this.calculateOvertime = calculateOvertime; }

    public Integer getMinWorkingHoursPerDay() { return minWorkingHoursPerDay; }
    public void setMinWorkingHoursPerDay(Integer minWorkingHoursPerDay) { this.minWorkingHoursPerDay = minWorkingHoursPerDay; }

    public Integer getPreventDuplicatePunchMins() { return preventDuplicatePunchMins; }
    public void setPreventDuplicatePunchMins(Integer preventDuplicatePunchMins) { this.preventDuplicatePunchMins = preventDuplicatePunchMins; }

    public Boolean getRequireServerTimeValidation() { return requireServerTimeValidation; }
    public void setRequireServerTimeValidation(Boolean requireServerTimeValidation) { this.requireServerTimeValidation = requireServerTimeValidation; }

    public Boolean getKioskModeEnabled() { return kioskModeEnabled; }
    public void setKioskModeEnabled(Boolean kioskModeEnabled) { this.kioskModeEnabled = kioskModeEnabled; }

    public Boolean getPreventScreenLock() { return preventScreenLock; }
    public void setPreventScreenLock(Boolean preventScreenLock) { this.preventScreenLock = preventScreenLock; }

    public Boolean getAppAutoLaunch() { return appAutoLaunch; }
    public void setAppAutoLaunch(Boolean appAutoLaunch) { this.appAutoLaunch = appAutoLaunch; }

    public Boolean getDeviceAuthRequired() { return deviceAuthRequired; }
    public void setDeviceAuthRequired(Boolean deviceAuthRequired) { this.deviceAuthRequired = deviceAuthRequired; }

    public String getSecureDeviceToken() { return secureDeviceToken; }
    public void setSecureDeviceToken(String secureDeviceToken) { this.secureDeviceToken = secureDeviceToken; }

    public Integer getDeviceTokenExpirationDays() { return deviceTokenExpirationDays; }
    public void setDeviceTokenExpirationDays(Integer deviceTokenExpirationDays) { this.deviceTokenExpirationDays = deviceTokenExpirationDays; }

    public Integer getSessionTimeoutMins() { return sessionTimeoutMins; }
    public void setSessionTimeoutMins(Integer sessionTimeoutMins) { this.sessionTimeoutMins = sessionTimeoutMins; }

    public Integer getMaxFailedKioskAttempts() { return maxFailedKioskAttempts; }
    public void setMaxFailedKioskAttempts(Integer maxFailedKioskAttempts) { this.maxFailedKioskAttempts = maxFailedKioskAttempts; }

    public Integer getTempLockoutDurationMins() { return tempLockoutDurationMins; }
    public void setTempLockoutDurationMins(Integer tempLockoutDurationMins) { this.tempLockoutDurationMins = tempLockoutDurationMins; }

    public Boolean getRequireHttps() { return requireHttps; }
    public void setRequireHttps(Boolean requireHttps) { this.requireHttps = requireHttps; }

    public Boolean getQrScannerEnabled() { return qrScannerEnabled; }
    public void setQrScannerEnabled(Boolean qrScannerEnabled) { this.qrScannerEnabled = qrScannerEnabled; }

    public Integer getQrExpirationMins() { return qrExpirationMins; }
    public void setQrExpirationMins(Integer qrExpirationMins) { this.qrExpirationMins = qrExpirationMins; }

    public Boolean getQrRegenerationAllowed() { return qrRegenerationAllowed; }
    public void setQrRegenerationAllowed(Boolean qrRegenerationAllowed) { this.qrRegenerationAllowed = qrRegenerationAllowed; }

    public String getQrSigningSecret() { return qrSigningSecret; }
    public void setQrSigningSecret(String qrSigningSecret) { this.qrSigningSecret = qrSigningSecret; }

    public Boolean getNotifySuccessCheckin() { return notifySuccessCheckin; }
    public void setNotifySuccessCheckin(Boolean notifySuccessCheckin) { this.notifySuccessCheckin = notifySuccessCheckin; }

    public Boolean getNotifySuccessCheckout() { return notifySuccessCheckout; }
    public void setNotifySuccessCheckout(Boolean notifySuccessCheckout) { this.notifySuccessCheckout = notifySuccessCheckout; }

    public Boolean getNotifyFailedFace() { return notifyFailedFace; }
    public void setNotifyFailedFace(Boolean notifyFailedFace) { this.notifyFailedFace = notifyFailedFace; }

    public Boolean getNotifyGpsFailure() { return notifyGpsFailure; }
    public void setNotifyGpsFailure(Boolean notifyGpsFailure) { this.notifyGpsFailure = notifyGpsFailure; }

    public Boolean getNotifyDeviceDisconnected() { return notifyDeviceDisconnected; }
    public void setNotifyDeviceDisconnected(Boolean notifyDeviceDisconnected) { this.notifyDeviceDisconnected = notifyDeviceDisconnected; }

    public Boolean getNotifyCameraError() { return notifyCameraError; }
    public void setNotifyCameraError(Boolean notifyCameraError) { this.notifyCameraError = notifyCameraError; }

    public Boolean getNotifyRepeatedFailures() { return notifyRepeatedFailures; }
    public void setNotifyRepeatedFailures(Boolean notifyRepeatedFailures) { this.notifyRepeatedFailures = notifyRepeatedFailures; }

    public String getAdminAlertEmail() { return adminAlertEmail; }
    public void setAdminAlertEmail(String adminAlertEmail) { this.adminAlertEmail = adminAlertEmail; }

    public Boolean getAutoSyncEnabled() { return autoSyncEnabled; }
    public void setAutoSyncEnabled(Boolean autoSyncEnabled) { this.autoSyncEnabled = autoSyncEnabled; }

    public Integer getSyncRetryIntervalSecs() { return syncRetryIntervalSecs; }
    public void setSyncRetryIntervalSecs(Integer syncRetryIntervalSecs) { this.syncRetryIntervalSecs = syncRetryIntervalSecs; }

    public Boolean getOfflinePunchStorageEnabled() { return offlinePunchStorageEnabled; }
    public void setOfflinePunchStorageEnabled(Boolean offlinePunchStorageEnabled) { this.offlinePunchStorageEnabled = offlinePunchStorageEnabled; }

    public Integer getBiometricRetentionDays() { return biometricRetentionDays; }
    public void setBiometricRetentionDays(Integer biometricRetentionDays) { this.biometricRetentionDays = biometricRetentionDays; }

    public Boolean getEncryptBiometricTemplates() { return encryptBiometricTemplates; }
    public void setEncryptBiometricTemplates(Boolean encryptBiometricTemplates) { this.encryptBiometricTemplates = encryptBiometricTemplates; }

    public Boolean getEnableAuditLogging() { return enableAuditLogging; }
    public void setEnableAuditLogging(Boolean enableAuditLogging) { this.enableAuditLogging = enableAuditLogging; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
