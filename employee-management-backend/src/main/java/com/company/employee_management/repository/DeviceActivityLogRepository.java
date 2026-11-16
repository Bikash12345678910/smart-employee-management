package com.company.employee_management.repository;

import com.company.employee_management.entity.DeviceActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceActivityLogRepository extends JpaRepository<DeviceActivityLog, Long> {
    List<DeviceActivityLog> findTop50ByOrderByCreatedAtDesc();
    List<DeviceActivityLog> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);
}
