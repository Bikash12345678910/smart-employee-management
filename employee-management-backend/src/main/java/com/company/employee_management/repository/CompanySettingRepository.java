package com.company.employee_management.repository;

import com.company.employee_management.entity.CompanySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanySettingRepository extends JpaRepository<CompanySetting, Long> {
}
