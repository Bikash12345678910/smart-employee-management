package com.company.employee_management.repository;

import com.company.employee_management.entity.EmployeeBreak;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeBreakRepository extends JpaRepository<EmployeeBreak, Long> {

    List<EmployeeBreak> findByAttendanceId(Long attendanceId);

    List<EmployeeBreak> findByEmployeeIdAndBreakDate(Long employeeId, LocalDate date);

    List<EmployeeBreak> findByEmployeeIdOrderByBreakDateDescBreakStartTimeDesc(Long employeeId);

    @Query("SELECT b FROM EmployeeBreak b WHERE " +
           "(:date IS NULL OR b.breakDate = :date) AND " +
           "(:employeeId IS NULL OR b.employee.id = :employeeId)")
    Page<EmployeeBreak> findWithFilters(@Param("date") LocalDate date,
                                        @Param("employeeId") Long employeeId,
                                        Pageable pageable);

    @Query("SELECT b FROM EmployeeBreak b WHERE " +
           "b.breakDate BETWEEN :startDate AND :endDate AND " +
           "(:employeeId IS NULL OR b.employee.id = :employeeId) ORDER BY b.breakDate DESC, b.breakStartTime DESC")
    List<EmployeeBreak> findForMonthlyReport(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            @Param("employeeId") Long employeeId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM EmployeeBreak b WHERE b.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Long employeeId);
}
