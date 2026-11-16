package com.company.employee_management.repository;

import com.company.employee_management.entity.MobileBiometricSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MobileBiometricSettingsRepository extends JpaRepository<MobileBiometricSettings, Long> {
}
