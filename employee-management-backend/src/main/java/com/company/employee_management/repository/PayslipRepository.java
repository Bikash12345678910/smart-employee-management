package com.company.employee_management.repository;

import com.company.employee_management.entity.Payslip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    Optional<Payslip> findByEmployeeIdAndSalaryMonthAndSalaryYear(Long employeeId, Integer month, Integer year);

    Optional<Payslip> findByPayslipNumber(String payslipNumber);

    List<Payslip> findByEmployeeIdOrderBySalaryYearDescSalaryMonthDesc(Long employeeId);

    @Query("SELECT p FROM Payslip p WHERE " +
           "(:month IS NULL OR p.salaryMonth = :month) AND " +
           "(:year IS NULL OR p.salaryYear = :year) AND " +
           "(:employeeId IS NULL OR p.employee.id = :employeeId)")
    Page<Payslip> findWithFilters(@Param("month") Integer month,
                                  @Param("year") Integer year,
                                  @Param("employeeId") Long employeeId,
                                  Pageable pageable);

    @Query("SELECT SUM(p.netPayableSalary) FROM Payslip p WHERE p.salaryMonth = :month AND p.salaryYear = :year")
    java.math.BigDecimal getTotalSalaryPayoutForMonth(@Param("month") Integer month, @Param("year") Integer year);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM Payslip p WHERE p.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Long employeeId);
}
