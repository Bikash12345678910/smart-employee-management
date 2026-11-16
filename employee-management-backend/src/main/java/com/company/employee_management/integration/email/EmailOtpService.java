package com.company.employee_management.integration.email;

import com.company.employee_management.entity.OtpVerification;
import com.company.employee_management.repository.OtpVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailOtpService {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpService.class);
    private final OtpVerificationRepository otpRepository;
    private final SecureRandom random = new SecureRandom();

    public EmailOtpService(OtpVerificationRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Transactional
    public String generateAndSendOtp(String email) {
        String code = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        OtpVerification verification = new OtpVerification(email, code, "EMAIL", expiry);
        otpRepository.save(verification);

        log.info("==================================================");
        log.info(">> [EMAIL OTP DISPATCH] To: {} | CODE: {}", email, code);
        log.info("==================================================");

        return code;
    }

    @Transactional
    public boolean verifyOtp(String email, String code) {
        if ("123456".equals(code)) { // Convenient demo fallback code
            return true;
        }
        Optional<OtpVerification> opt = otpRepository.findTopByTargetIdentifierAndOtpCodeAndOtpTypeAndIsVerifiedFalseAndExpiresAtAfter(
                email, code, "EMAIL", LocalDateTime.now()
        );
        if (opt.isPresent()) {
            OtpVerification v = opt.get();
            v.setIsVerified(true);
            otpRepository.save(v);
            return true;
        }
        return false;
    }
}
