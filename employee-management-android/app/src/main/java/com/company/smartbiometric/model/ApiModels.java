package com.company.smartbiometric.model;

import java.util.List;

public class ApiModels {

    public static class ApiResponse<T> {
        public boolean success;
        public String message;
        public T data;
        public String timestamp;
    }

    public static class LoginRequest {
        public String username;
        public String password;
        public boolean rememberMe = true;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class AuthData {
        public String token;
        public String tokenType;
        public String username;
        public String email;
        public String role;
    }

    public static class TerminalConfig {
        public String terminalCode;
        public String terminalName;
        public String deviceModel;
        public String companyName;
        public Integer tapTimeoutMs;
        public Double faceMatchThreshold;
        public Boolean livenessRequired;
        public Boolean biometricPromptEnabled;
        public Boolean kioskModeEnabled;
        public Long serverTimeEpochMs;
    }

    public static class TerminalEmployee {
        public Long id;
        public String employeeCode;
        public String fullName;
        public String email;
        public String phoneNumber;
        public Boolean isActive;
        public Boolean faceEnrolled;
        public Boolean consentGiven;
        public String todayStatus;
        public String checkInTime;
        public String checkOutTime;

        @Override
        public String toString() {
            String badge = Boolean.TRUE.equals(faceEnrolled) ? "[Face OK]" : "[No Face]";
            return employeeCode + " - " + fullName + " " + badge;
        }
    }

    public static class PunchRequest {
        public String employeeCode;
        public String action; // CHECK_IN or CHECK_OUT
        public double[] faceVector;
        public Boolean livenessPassed;
        public Double livenessScore;
        public String livenessChallenge;
        public Boolean biometricPromptUsed;
        public String verificationMethod;
        public String nonce;
        public Long clientTimestampMs;
    }

    public static class PunchResponse {
        public boolean verified;
        public String action;
        public String employeeCode;
        public String employeeName;
        public String attendanceDate;
        public String serverTimestamp;
        public String checkInTime;
        public String checkOutTime;
        public Long workingMinutes;
        public Double matchScore;
        public String verificationStatus;
        public String message;
    }

    public static class EnrollmentRequest {
        public String employeeCode;
        public double[] faceVector;
        public Boolean consentGiven;
        public String consentTextVersion;
        public Double qualityScore;
        public String enrolledByAdmin;
    }

    public static class DailyReportSummary {
        public String date;
        public int totalEmployees;
        public int presentCount;
        public int absentCount;
        public int lateCount;
        public int checkedOutCount;
        public List<DailyReportRow> records;
    }

    public static class DailyReportRow {
        public String employeeCode;
        public String employeeName;
        public String status;
        public String checkInTime;
        public String checkOutTime;
        public Long workingMinutes;
    }
}
