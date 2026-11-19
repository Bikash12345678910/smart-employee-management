package com.company.employee_management.integration.email;

import com.company.employee_management.entity.Employee;
import com.company.employee_management.entity.OtpVerification;
import com.company.employee_management.repository.EmployeeRepository;
import com.company.employee_management.repository.OtpVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailOtpService {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpService.class);
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final OtpVerificationRepository otpRepository;
    private final EmployeeRepository employeeRepository;
    private final SecureRandom random = new SecureRandom();

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String gmailUsername;

    @Value("${spring.mail.password:}")
    private String gmailAppPassword;

    public EmailOtpService(OtpVerificationRepository otpRepository, EmployeeRepository employeeRepository) {
        this.otpRepository = otpRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public String generateAndSendOtp(String email) {
        if (email == null || !email.trim().contains("@")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        String code = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        OtpVerification verification = new OtpVerification(normalizedEmail, code, "EMAIL", expiry);
        otpRepository.save(verification);

        boolean sentViaGmail = false;
        if (mailSender != null && gmailUsername != null && !gmailUsername.isBlank()
                && gmailAppPassword != null && !gmailAppPassword.isBlank()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(gmailUsername);
                message.setTo(normalizedEmail);
                message.setSubject("Your Verification OTP - Smart Employee Attendance System");
                message.setText("Hello,\n\nYour 6-digit email verification OTP is: " + code
                        + "\n\nThis code is valid for " + OTP_EXPIRY_MINUTES + " minutes. "
                        + "Do not share this OTP with anyone.\n\nRegards,\nSmart Employee Attendance System");
                mailSender.send(message);
                sentViaGmail = true;
                log.info(">> [GMAIL SMTP OTP SENT] To: {}", normalizedEmail);
            } catch (Exception ex) {
                log.error(">> [GMAIL SMTP ERROR] Failed to send email to {}: {}", normalizedEmail, ex.getMessage());
            }
        }

        if (!sentViaGmail) {
            log.info("=========================================================");
            log.info(">> [EMAIL OTP - DEV/FALLBACK] To: {} | 6-DIGIT OTP: {}", normalizedEmail, code);
            log.info(">> Set GMAIL_USERNAME & GMAIL_APP_PASSWORD for live Gmail SMTP delivery.");
            log.info("=========================================================");
        }

        return code;
    }

    public boolean isGmailConfigured() {
        return mailSender != null && gmailUsername != null && !gmailUsername.isBlank()
                && gmailAppPassword != null && !gmailAppPassword.isBlank();
    }

    @Transactional
    public boolean verifyOtp(String email, String code) {
        if (email == null || code == null || code.trim().length() != 6) {
            return false;
        }
        String normalizedEmail = email.trim().toLowerCase();
        Optional<OtpVerification> opt = otpRepository
                .findTopByTargetIdentifierAndOtpCodeAndOtpTypeAndIsVerifiedFalseAndExpiresAtAfter(
                        normalizedEmail, code.trim(), "EMAIL", LocalDateTime.now());

        if (opt.isPresent()) {
            OtpVerification v = opt.get();
            v.setIsVerified(true);
            otpRepository.save(v);

            // Mark employee email verified in MySQL if employee record exists
            employeeRepository.findByEmail(normalizedEmail).ifPresent(emp -> {
                emp.setIsEmailVerified(true);
                employeeRepository.save(emp);
            });
            return true;
        }
        return false;
    }
}
