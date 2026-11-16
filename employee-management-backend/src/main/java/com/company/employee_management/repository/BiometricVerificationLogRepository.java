package com.company.employee_management.repository;

import com.company.employee_management.entity.BiometricVerificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BiometricVerificationLogRepository extends JpaRepository<BiometricVerificationLog, Long> {
    List<BiometricVerificationLog> findTop50ByOrderByCapturedAtDesc();
    List<BiometricVerificationLog> findByVerificationTypeOrderByCapturedAtDesc(String verificationType);
    long countByStatus(String status);
}
