package com.company.employee_management.repository;

import com.company.employee_management.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByTargetIdentifierAndOtpTypeOrderByCreatedAtDesc(String targetIdentifier, String otpType);
    Optional<OtpVerification> findTopByTargetIdentifierAndOtpCodeAndOtpTypeAndIsVerifiedFalseAndExpiresAtAfter(
            String targetIdentifier, String otpCode, String otpType, LocalDateTime now);
}
