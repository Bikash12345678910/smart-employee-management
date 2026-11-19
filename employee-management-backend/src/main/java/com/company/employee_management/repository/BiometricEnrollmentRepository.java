package com.company.employee_management.repository;

import com.company.employee_management.entity.BiometricEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BiometricEnrollmentRepository extends JpaRepository<BiometricEnrollment, Long> {
    Optional<BiometricEnrollment> findByEmployeeIdAndModalityAndIsActiveTrue(Long employeeId, String modality);
    Optional<BiometricEnrollment> findByEmployeeCodeAndModalityAndIsActiveTrue(String employeeCode, String modality);
    Optional<BiometricEnrollment> findByEmployeeIdAndModality(Long employeeId, String modality);
    List<BiometricEnrollment> findByIsActiveTrue();
}
