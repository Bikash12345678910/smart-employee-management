package com.company.employee_management.repository;

import com.company.employee_management.entity.BiometricRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BiometricRegistrationRepository extends JpaRepository<BiometricRegistration, Long> {
    List<BiometricRegistration> findByEmployeeId(Long employeeId);
    List<BiometricRegistration> findByDeviceId(Long deviceId);
    Optional<BiometricRegistration> findByEmployeeIdAndBiometricType(Long employeeId, String biometricType);
    Optional<BiometricRegistration> findByTemplateHash(String templateHash);
    boolean existsByEmployeeIdAndBiometricType(Long employeeId, String biometricType);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM BiometricRegistration b WHERE b.employee.id = :employeeId")
    void deleteByEmployeeId(@org.springframework.data.repository.query.Param("employeeId") Long employeeId);
}
