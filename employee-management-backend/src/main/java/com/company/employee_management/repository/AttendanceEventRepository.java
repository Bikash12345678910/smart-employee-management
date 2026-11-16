package com.company.employee_management.repository;

import com.company.employee_management.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findTop20ByOrderByEventTimeDesc();
    List<AttendanceEvent> findByEmployeeIdOrderByEventTimeDesc(Long employeeId);
    List<AttendanceEvent> findByEventTimeBetweenOrderByEventTimeDesc(LocalDateTime start, LocalDateTime end);
    List<AttendanceEvent> findByEmployeeIdAndEventTimeBetweenOrderByEventTimeAsc(Long employeeId, LocalDateTime start, LocalDateTime end);
    java.util.Optional<AttendanceEvent> findTopByEmployeeIdAndEventTimeBetweenOrderByEventTimeDesc(Long employeeId, LocalDateTime start, LocalDateTime end);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM AttendanceEvent e WHERE e.employee.id = :employeeId")
    void deleteByEmployeeId(@org.springframework.data.repository.query.Param("employeeId") Long employeeId);
}
