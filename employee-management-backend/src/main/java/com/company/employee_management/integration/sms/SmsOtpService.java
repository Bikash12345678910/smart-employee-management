package com.company.employee_management.integration.sms;

import com.company.employee_management.entity.Employee;
import com.company.employee_management.entity.OtpVerification;
import com.company.employee_management.repository.EmployeeRepository;
import com.company.employee_management.repository.OtpVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class SmsOtpService {

    private static final Logger log = LoggerFactory.getLogger(SmsOtpService.class);
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final OtpVerificationRepository otpRepository;
    private final EmployeeRepository employeeRepository;
    private final SecureRandom random = new SecureRandom();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.sms.provider:CONSOLE}")
    private String smsProvider;

    @Value("${app.sms.fast2sms.api-key:}")
    private String fast2SmsApiKey;

    @Value("${app.sms.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${app.sms.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${app.sms.twilio.from-number:}")
    private String twilioFromNumber;

    public SmsOtpService(OtpVerificationRepository otpRepository, EmployeeRepository employeeRepository) {
        this.otpRepository = otpRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public String generateAndSendOtp(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().length() < 8) {
            throw new IllegalArgumentException("Please enter a valid mobile number.");
        }
        String normalizedPhone = phoneNumber.trim();
        String code = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        OtpVerification verification = new OtpVerification(normalizedPhone, code, "PHONE", expiry);
        otpRepository.save(verification);

        boolean dispatchedViaGateway = false;

        if ("FAST2SMS".equalsIgnoreCase(smsProvider) && fast2SmsApiKey != null && !fast2SmsApiKey.isBlank()) {
            dispatchedViaGateway = sendViaFast2Sms(normalizedPhone, code);
        } else if ("TWILIO".equalsIgnoreCase(smsProvider)
                && twilioAccountSid != null && !twilioAccountSid.isBlank()
                && twilioAuthToken != null && !twilioAuthToken.isBlank()
                && twilioFromNumber != null && !twilioFromNumber.isBlank()) {
            dispatchedViaGateway = sendViaTwilio(normalizedPhone, code);
        }

        if (!dispatchedViaGateway) {
            log.info("=========================================================");
            log.info(">> [SMS OTP - DEV/FALLBACK] To: {} | 6-DIGIT OTP: {}", normalizedPhone, code);
            log.info(">> Configure SMS_PROVIDER=FAST2SMS (FAST2SMS_API_KEY) or TWILIO for live SMS delivery.");
            log.info("=========================================================");
        }

        return code;
    }

    private boolean sendViaFast2Sms(String phone, String code) {
        try {
            // Strip non-digits and take last 10 digits for Indian mobile numbers
            String digits = phone.replaceAll("\\D+", "");
            if (digits.length() > 10) {
                digits = digits.substring(digits.length() - 10);
            }
            String jsonBody = String.format(
                    "{\"route\":\"otp\",\"variables_values\":\"%s\",\"numbers\":\"%s\"}",
                    code, digits);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.fast2sms.com/dev/bulkV2"))
                    .timeout(Duration.ofSeconds(10))
                    .header("authorization", fast2SmsApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info(">> [FAST2SMS OTP SENT] To: {} | Status: {}", digits, response.statusCode());
                return true;
            }
            log.error(">> [FAST2SMS ERROR] HTTP {} | Body: {}", response.statusCode(), response.body());
        } catch (Exception ex) {
            log.error(">> [FAST2SMS EXCEPTION] {}", ex.getMessage());
        }
        return false;
    }

    private boolean sendViaTwilio(String phone, String code) {
        try {
            String messageText = "Your Smart Employee Attendance 6-digit OTP is: " + code + ". Valid for 5 minutes.";
            String form = "To=" + URLEncoder.encode(phone, StandardCharsets.UTF_8)
                    + "&From=" + URLEncoder.encode(twilioFromNumber, StandardCharsets.UTF_8)
                    + "&Body=" + URLEncoder.encode(messageText, StandardCharsets.UTF_8);

            String basicAuth = Base64.getEncoder().encodeToString(
                    (twilioAccountSid + ":" + twilioAuthToken).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Basic " + basicAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info(">> [TWILIO SMS OTP SENT] To: {} | Status: {}", phone, response.statusCode());
                return true;
            }
            log.error(">> [TWILIO ERROR] HTTP {} | Body: {}", response.statusCode(), response.body());
        } catch (Exception ex) {
            log.error(">> [TWILIO EXCEPTION] {}", ex.getMessage());
        }
        return false;
    }

    public String getActiveGatewayName() {
        if ("FAST2SMS".equalsIgnoreCase(smsProvider) && fast2SmsApiKey != null && !fast2SmsApiKey.isBlank()) {
            return "FAST2SMS";
        }
        if ("TWILIO".equalsIgnoreCase(smsProvider) && twilioAccountSid != null && !twilioAccountSid.isBlank()) {
            return "TWILIO";
        }
        return "CONSOLE_FALLBACK";
    }

    @Transactional
    public boolean verifyOtp(String phoneNumber, String code) {
        if (phoneNumber == null || code == null || code.trim().length() != 6) {
            return false;
        }
        String normalizedPhone = phoneNumber.trim();
        Optional<OtpVerification> opt = otpRepository
                .findTopByTargetIdentifierAndOtpCodeAndOtpTypeAndIsVerifiedFalseAndExpiresAtAfter(
                        normalizedPhone, code.trim(), "PHONE", LocalDateTime.now());

        if (opt.isPresent()) {
            OtpVerification v = opt.get();
            v.setIsVerified(true);
            otpRepository.save(v);

            // Mark employee phone verified in MySQL if employee record exists
            employeeRepository.findByPhoneNumber(normalizedPhone).ifPresent(emp -> {
                emp.setIsPhoneVerified(true);
                employeeRepository.save(emp);
            });
            return true;
        }
        return false;
    }
}
