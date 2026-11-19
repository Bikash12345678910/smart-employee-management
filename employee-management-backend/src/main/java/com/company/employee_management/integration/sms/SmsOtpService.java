package com.company.employee_management.integration.sms;

import com.company.employee_management.entity.OtpVerification;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SmsOtpService {

    private static final Logger log = LoggerFactory.getLogger(SmsOtpService.class);
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final OtpVerificationRepository otpRepository;
    private final SecureRandom random = new SecureRandom();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.sms.provider:CONSOLE}")
    private String defaultSmsProvider;

    @Value("${app.sms.fast2sms.api-key:}")
    private String defaultFast2SmsKey;

    @Value("${app.sms.twilio.account-sid:}")
    private String defaultTwilioSid;

    @Value("${app.sms.twilio.auth-token:}")
    private String defaultTwilioToken;

    @Value("${app.sms.twilio.from-number:}")
    private String defaultTwilioFrom;

    public SmsOtpService(OtpVerificationRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Transactional
    public String generateAndSendOtp(String phoneNumber) {
        Map<String, Object> res = generateAndSendOtpWithConfig(phoneNumber, null, null, null, null, null);
        return (String) res.get("code");
    }

    @Transactional
    public Map<String, Object> generateAndSendOtpWithConfig(
            String phoneNumber,
            String smsProvider,
            String fast2SmsApiKey,
            String twilioAccountSid,
            String twilioAuthToken,
            String twilioFromNumber) {

        if (phoneNumber == null || phoneNumber.trim().length() < 8) {
            throw new IllegalArgumentException("Please enter a valid mobile number.");
        }

        String targetPhone = phoneNumber.trim();
        String code = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        OtpVerification verification = new OtpVerification(targetPhone, code, "PHONE", expiry);
        otpRepository.save(verification);

        String provider = (smsProvider != null && !smsProvider.isBlank()) ? smsProvider.trim().toUpperCase() : defaultSmsProvider;
        String f2sKey = (fast2SmsApiKey != null && !fast2SmsApiKey.isBlank()) ? fast2SmsApiKey.trim() : defaultFast2SmsKey;
        String twSid = (twilioAccountSid != null && !twilioAccountSid.isBlank()) ? twilioAccountSid.trim() : defaultTwilioSid;
        String twToken = (twilioAuthToken != null && !twilioAuthToken.isBlank()) ? twilioAuthToken.trim() : defaultTwilioToken;
        String twFrom = (twilioFromNumber != null && !twilioFromNumber.isBlank()) ? twilioFromNumber.trim() : defaultTwilioFrom;

        Map<String, Object> result = new HashMap<>();
        result.put("target", targetPhone);
        result.put("expiresInMinutes", OTP_EXPIRY_MINUTES);

        if ("FAST2SMS".equalsIgnoreCase(provider) && f2sKey != null && !f2sKey.isBlank()) {
            sendViaFast2Sms(targetPhone, code, f2sKey);
            result.put("deliveredLive", true);
            result.put("channel", "FAST2SMS");
            result.put("message", "6-digit SMS OTP sent directly to " + targetPhone + " via Fast2SMS!");
            return result;
        }

        if ("TWILIO".equalsIgnoreCase(provider)
                && twSid != null && !twSid.isBlank()
                && twToken != null && !twToken.isBlank()
                && twFrom != null && !twFrom.isBlank()) {
            sendViaTwilio(targetPhone, code, twSid, twToken, twFrom);
            result.put("deliveredLive", true);
            result.put("channel", "TWILIO");
            result.put("message", "6-digit SMS OTP sent directly to " + targetPhone + " via Twilio!");
            return result;
        }

        log.info("=========================================================");
        log.info(">> [SMS OTP - NOT CONFIGURED] To: {} | 6-DIGIT OTP: {}", targetPhone, code);
        log.info("=========================================================");
        result.put("deliveredLive", false);
        result.put("channel", "SERVER_LOG");
        result.put("code", code);
        result.put("message", "Enter your Fast2SMS API Key or Twilio credentials in OTP Gateway Settings to send real SMS directly to the phone.");
        return result;
    }

    private void sendViaFast2Sms(String phone, String code, String apiKey) {
        try {
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
                    .header("authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalArgumentException("Fast2SMS rejected request (HTTP " + response.statusCode() + "): " + response.body());
            }
            log.info(">> [FAST2SMS SENT] 6-digit OTP sent to {}", digits);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Fast2SMS SMS failed: " + ex.getMessage());
        }
    }

    private void sendViaTwilio(String phone, String code, String sid, String token, String fromNumber) {
        try {
            String formattedTo = phone.startsWith("+") ? phone : ("+91" + phone.replaceAll("\\D+", ""));
            String bodyText = "Your Smart Employee Management 6-digit verification OTP is: " + code + ". Valid for 5 minutes.";
            String form = "To=" + URLEncoder.encode(formattedTo, StandardCharsets.UTF_8)
                    + "&From=" + URLEncoder.encode(fromNumber, StandardCharsets.UTF_8)
                    + "&Body=" + URLEncoder.encode(bodyText, StandardCharsets.UTF_8);

            String basicAuth = Base64.getEncoder().encodeToString((sid + ":" + token).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + sid + "/Messages.json"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Basic " + basicAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalArgumentException("Twilio rejected request (HTTP " + response.statusCode() + "): " + response.body());
            }
            log.info(">> [TWILIO SENT] 6-digit OTP sent to {}", formattedTo);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Twilio SMS failed: " + ex.getMessage());
        }
    }

    @Transactional
    public boolean verifyOtp(String phoneNumber, String code) {
        if (phoneNumber == null || code == null || code.trim().length() != 6) {
            return false;
        }
        String targetPhone = phoneNumber.trim();
        Optional<OtpVerification> opt = otpRepository
                .findTopByTargetIdentifierAndOtpCodeAndOtpTypeAndIsVerifiedFalseAndExpiresAtAfter(
                        targetPhone, code.trim(), "PHONE", LocalDateTime.now());
        if (opt.isPresent()) {
            OtpVerification v = opt.get();
            v.setIsVerified(true);
            otpRepository.save(v);
            return true;
        }
        return false;
    }
}
