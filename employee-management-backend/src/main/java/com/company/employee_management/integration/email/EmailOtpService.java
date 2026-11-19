package com.company.employee_management.integration.email;

import com.company.employee_management.entity.OtpVerification;
import com.company.employee_management.repository.OtpVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Service
public class EmailOtpService {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpService.class);
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final OtpVerificationRepository otpRepository;
    private final SecureRandom random = new SecureRandom();

    @Value("${spring.mail.username:}")
    private String defaultGmailUsername;

    @Value("${spring.mail.password:}")
    private String defaultGmailAppPassword;

    public EmailOtpService(OtpVerificationRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Transactional
    public String generateAndSendOtp(String email) {
        Map<String, Object> result = generateAndSendOtpWithConfig(email, null, null);
        return (String) result.get("code");
    }

    @Transactional
    public Map<String, Object> generateAndSendOtpWithConfig(String email, String senderEmail, String appPassword) {
        if (email == null || !email.trim().contains("@")) {
            throw new IllegalArgumentException("Please enter a valid employee Gmail / email address.");
        }

        String targetEmail = email.trim().toLowerCase();
        String code = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        OtpVerification verification = new OtpVerification(targetEmail, code, "EMAIL", expiry);
        otpRepository.save(verification);

        String effectiveUser = (senderEmail != null && !senderEmail.isBlank()) ? senderEmail.trim() : defaultGmailUsername;
        String effectivePass = (appPassword != null && !appPassword.isBlank()) ? appPassword.replaceAll("\\s+", "") : defaultGmailAppPassword;

        Map<String, Object> result = new HashMap<>();
        result.put("target", targetEmail);
        result.put("expiresInMinutes", OTP_EXPIRY_MINUTES);

        if (effectiveUser != null && !effectiveUser.isBlank() && effectivePass != null && !effectivePass.isBlank()) {
            try {
                JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
                mailSender.setHost("smtp.gmail.com");
                mailSender.setPort(587);
                mailSender.setUsername(effectiveUser);
                mailSender.setPassword(effectivePass);

                Properties props = mailSender.getJavaMailProperties();
                props.put("mail.transport.protocol", "smtp");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.connectiontimeout", "8000");
                props.put("mail.smtp.timeout", "8000");
                props.put("mail.smtp.writetimeout", "8000");

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(effectiveUser);
                message.setTo(targetEmail);
                message.setSubject("Email Verification OTP - Smart Employee Management System");
                message.setText("Hello,\n\nYour 6-digit email verification OTP is: " + code
                        + "\n\nThis OTP is valid for " + OTP_EXPIRY_MINUTES + " minutes.\n\n"
                        + "Regards,\nSmart Employee Management System");

                mailSender.send(message);
                log.info(">> [GMAIL SMTP SENT] 6-digit OTP delivered to {}", targetEmail);

                result.put("deliveredLive", true);
                result.put("channel", "GMAIL_SMTP");
                result.put("message", "6-digit OTP sent directly to " + targetEmail + " via Gmail!");
                return result;
            } catch (Exception ex) {
                log.error(">> [GMAIL SMTP FAILED] To {}: {}", targetEmail, ex.getMessage());
                throw new IllegalArgumentException("Gmail SMTP failed: " + ex.getMessage()
                        + ". Please verify your Sender Gmail and 16-character Google App Password.");
            }
        }

        // If no Gmail credentials were provided yet, log on server and instruct user
        log.info("=========================================================");
        log.info(">> [EMAIL OTP - NOT CONFIGURED] To: {} | 6-DIGIT OTP: {}", targetEmail, code);
        log.info("=========================================================");
        result.put("deliveredLive", false);
        result.put("channel", "SERVER_LOG");
        result.put("code", code);
        result.put("message", "Enter your Sender Gmail & 16-char Google App Password in OTP Gateway Settings to send directly to Gmail inbox.");
        return result;
    }

    @Transactional
    public boolean verifyOtp(String email, String code) {
        if (email == null || code == null || code.trim().length() != 6) {
            return false;
        }
        String targetEmail = email.trim().toLowerCase();
        Optional<OtpVerification> opt = otpRepository
                .findTopByTargetIdentifierAndOtpCodeAndOtpTypeAndIsVerifiedFalseAndExpiresAtAfter(
                        targetEmail, code.trim(), "EMAIL", LocalDateTime.now());
        if (opt.isPresent()) {
            OtpVerification v = opt.get();
            v.setIsVerified(true);
            otpRepository.save(v);
            return true;
        }
        return false;
    }
}
