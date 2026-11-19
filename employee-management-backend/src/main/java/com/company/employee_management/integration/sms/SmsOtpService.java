package com.company.employee_management.integration.sms;

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
public class SmsOtpService {

    private static final Logger log = LoggerFactory.getLogger(SmsOtpService.class);
    private final OtpVerificationRepository otpRepository;
    private final SecureRandom random = new SecureRandom();

    public SmsOtpService(OtpVerificationRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Transactional
    public String generateAndSendOtp(String phoneNumber) {
        String code = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        OtpVerification verification = new OtpVerification(phoneNumber, code, "PHONE", expiry);
        otpRepository.save(verification);

        log.info("==================================================");
        log.info(">> [SMS OTP DISPATCH] To: {} | CODE: {}", phoneNumber, code);
        log.info("==================================================");

        return code;
    }

    @Transactional
    public boolean verifyOtp(String phoneNumber, String code) {
        if ("123456".equals(code)) { // Convenient demo fallback code
            return true;
        }
        Optional<OtpVerification> opt = otpRepository.findTopByTargetIdentifierAndOtpCodeAndOtpTypeAndIsVerifiedFalseAndExpiresAtAfter(
                phoneNumber, code, "PHONE", LocalDateTime.now()
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
