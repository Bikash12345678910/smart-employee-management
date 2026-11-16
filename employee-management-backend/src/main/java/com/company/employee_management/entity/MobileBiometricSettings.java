package com.company.employee_management.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_biometric_settings")
public class MobileBiometricSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. Camera Configuration
    @Column(name = "camera_enabled", nullable = false)
    private Boolean cameraEnabled = true;

    @Column(name = "front_camera_selected", nullable = false)
    private Boolean frontCameraSelected = true;

    @Column(name = "face_detection_enabled", nullable = false)
    private Boolean faceDetectionEnabled = true;

    @Column(name = "min_face_quality", nullable = false)
    private Integer minFaceQuality = 85;

    @Column(name = "brightness_check_enabled", nullable = false)
    private Boolean brightnessCheckEnabled = true;

    @Column(name = "position_guidance_enabled", nullable = false)
    private Boolean positionGuidanceEnabled = true;

    @Column(name = "multiple_face_detection_prevented", nullable = false)
    private Boolean multipleFaceDetectionPrevented = true;

    // 2. Face Biometric Configuration
    @Column(name = "face_verification_enabled", nullable = false)
    private Boolean faceVerificationEnabled = true;

    @Column(name = "matching_threshold", nullable = false)
    private Integer matchingThreshold = 80;

    @Column(name = "liveness_detection_enabled", nullable = false)
    private Boolean livenessDetectionEnabled = true;

    @Column(name = "mismatch_alert_enabled", nullable = false)
    private Boolean mismatchAlertEnabled = true;

    @Column(name = "max_failed_attempts", nullable = false)
    private Integer maxFailedAttempts = 3;

    @Column(name = "verification_retry_seconds", nullable = false)
    private Integer verificationRetrySeconds = 30;

    @Column(name = "require_employee_consent", nullable = false)
    private Boolean requireEmployeeConsent = true;

    // 3. GPS & Location Configuration
    @Column(name = "gps_verification_enabled", nullable = false)
    private Boolean gpsVerificationEnabled = true;

    @Column(name = "office_name", nullable = false, length = 150)
    private String officeName = "Apex Global Tech HQ";

    @Column(name = "office_address", nullable = false)
    private String officeAddress = "100 Tech Park Boulevard, Suite 500, Silicon City";

    @Column(name = "office_latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal officeLatitude = new BigDecimal("37.7749290");

    @Column(name = "office_longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal officeLongitude = new BigDecimal("-122.4194160");

    @Column(name = "permitted_radius_meters", nullable = false)
    private Integer permittedRadiusMeters = 150;

    @Column(name = "gps_accuracy_requirement_meters", nullable = false)
    private Integer gpsAccuracyRequirementMeters = 25;

    // 4. Attendance Configuration
    @Column(name = "mobile_checkin_enabled", nullable = false)
    private Boolean mobileCheckinEnabled = true;

    @Column(name = "mobile_checkout_enabled", nullable = false)
    private Boolean mobileCheckoutEnabled = true;

    @Column(name = "allowed_checkin_start", nullable = false, length = 10)
    private String allowedCheckinStart = "07:30";

    @Column(name = "allowed_checkin_end", nullable = false, length = 10)
    private String allowedCheckinEnd = "11:00";

    @Column(name = "allowed_checkout_start", nullable = false, length = 10)
    private String allowedCheckoutStart = "17:00";

    @Column(name = "allowed_checkout_end", nullable = false, length = 10)
    private String allowedCheckoutEnd = "22:00";

    @Column(name = "attendance_grace_period_mins", nullable = false)
    private Integer attendanceGracePeriodMins = 15;

    @Column(name = "calculate_late_arrival", nullable = false)
    private Boolean calculateLateArrival = true;

    @Column(name = "calculate_early_departure", nullable = false)
    private Boolean calculateEarlyDeparture = true;

    @Column(name = "calculate_overtime", nullable = false)
    private Boolean calculateOvertime = true;

    @Column(name = "min_working_hours_per_day", nullable = false)
    private Integer minWorkingHoursPerDay = 8;

    @Column(name = "prevent_duplicate_punch_mins", nullable = false)
    private Integer preventDuplicatePunchMins = 5;

    @Column(name = "require_server_time_validation", nullable = false)
    private Boolean requireServerTimeValidation = true;

    // 5. Kiosk Security Configuration
    @Column(name = "kiosk_mode_enabled", nullable = false)
    private Boolean kioskModeEnabled = true;

    @Column(name = "prevent_screen_lock", nullable = false)
    private Boolean preventScreenLock = true;

    @Column(name = "app_auto_launch", nullable = false)
    private Boolean appAutoLaunch = true;

    @Column(name = "device_auth_required", nullable = false)
    private Boolean deviceAuthRequired = true;

    @Column(name = "secure_device_token", nullable = false)
    private String secureDeviceToken = "APEX-SEC-DEVICE-TOKEN-9843759247";

    @Column(name = "device_token_expiration_days", nullable = false)
    private Integer deviceTokenExpirationDays = 90;

    @Column(name = "session_timeout_mins", nullable = false)
    private Integer sessionTimeoutMins = 10;

    @Column(name = "max_failed_kiosk_attempts", nullable = false)
    private Integer maxFailedKioskAttempts = 5;

    @Column(name = "temp_lockout_duration_mins", nullable = false)
    private Integer tempLockoutDurationMins = 15;

    @Column(name = "require_https", nullable = false)
    private Boolean requireHttps = true;

    // 6. QR Code Configuration
    @Column(name = "qr_scanner_enabled", nullable = false)
    private Boolean qrScannerEnabled = true;

    @Column(name = "qr_expiration_mins", nullable = false)
    private Integer qrExpirationMins = 1440;

    @Column(name = "qr_regeneration_allowed", nullable = false)
    private Boolean qrRegenerationAllowed = true;

    @Column(name = "qr_signing_secret", nullable = false)
    private String qrSigningSecret = "APEX-QR-SIGNING-SECRET-KEY-9988";

    // 7. Notification Configuration
    @Column(name = "notify_success_checkin", nullable = false)
    private Boolean notifySuccessCheckin = true;

    @Column(name = "notify_success_checkout", nullable = false)
    private Boolean notifySuccessCheckout = true;

    @Column(name = "notify_failed_face", nullable = false)
    private Boolean notifyFailedFace = true;

    @Column(name = "notify_gps_failure", nullable = false)
    private Boolean notifyGpsFailure = true;

    @Column(name = "notify_device_disconnected", nullable = false)
    private Boolean notifyDeviceDisconnected = true;

    @Column(name = "notify_camera_error", nullable = false)
    private Boolean notifyCameraError = true;

    @Column(name = "notify_repeated_failures", nullable = false)
    private Boolean notifyRepeatedFailures = true;

    @Column(name = "admin_alert_email", nullable = false, length = 150)
    private String adminAlertEmail = "admin@apextech.com";

    // 8. Synchronization Configuration
    @Column(name = "auto_sync_enabled", nullable = false)
    private Boolean autoSyncEnabled = true;

    @Column(name = "sync_retry_interval_secs", nullable = false)
    private Integer syncRetryIntervalSecs = 60;

    @Column(name = "offline_punch_storage_enabled", nullable = false)
    private Boolean offlinePunchStorageEnabled = true;

    // 9. Privacy & Data Management
    @Column(name = "biometric_retention_days", nullable = false)
    private Integer biometricRetentionDays = 365;

    @Column(name = "encrypt_biometric_templates", nullable = false)
    private Boolean encryptBiometricTemplates = true;

    @Column(name = "enable_audit_logging", nullable = false)
    private Boolean enableAuditLogging = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 100)
    private String updatedBy = "admin";

    public MobileBiometricSettings() {}

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
