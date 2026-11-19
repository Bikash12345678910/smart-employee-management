package com.company.employee_management.repository;

import com.company.employee_management.entity.AttendanceTerminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceTerminalRepository extends JpaRepository<AttendanceTerminal, Long> {
    Optional<AttendanceTerminal> findByTerminalCode(String terminalCode);
}
