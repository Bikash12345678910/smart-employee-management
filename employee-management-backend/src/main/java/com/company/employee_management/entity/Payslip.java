package com.company.employee_management.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payslips", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "salary_month", "salary_year"})
})
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "payslip_number", nullable = false, unique = true, length = 50)
    private String payslipNumber;

    @Column(name = "salary_month", nullable = false)
    private Integer salaryMonth;

    @Column(name = "salary_year", nullable = false)
    private Integer salaryYear;

    @Column(name = "agreed_monthly_salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal agreedMonthlySalary;

    @Column(name = "total_working_days", nullable = false)
    private Integer totalWorkingDays = 30;

    @Column(name = "present_days", nullable = false)
    private Integer presentDays = 0;

    @Column(name = "absent_days", nullable = false)
    private Integer absentDays = 0;

    @Column(name = "leave_days", nullable = false)
    private Integer leaveDays = 0;

    @Column(name = "late_minutes", nullable = false)
    private Integer lateMinutes = 0;

    @Column(name = "total_working_hours", precision = 6, scale = 2, nullable = false)
    private BigDecimal totalWorkingHours = BigDecimal.ZERO;

    @Column(name = "gross_earnings", precision = 12, scale = 2, nullable = false)
    private BigDecimal grossEarnings = BigDecimal.ZERO;

    @Column(name = "total_deductions", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_payable_salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal netPayableSalary = BigDecimal.ZERO;

    @Column(length = 20)
    private String status = "FINALIZED"; // DRAFT, FINALIZED, PAID

    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "actual_working_minutes")
    private Integer actualWorkingMinutes = 0;

    @Column(name = "normal_working_minutes")
    private Integer normalWorkingMinutes = 0;

    @Column(name = "difference_minutes")
    private Integer differenceMinutes = 0;

    @Column(name = "salary_per_minute", precision = 12, scale = 4)
    private BigDecimal salaryPerMinute = BigDecimal.ZERO;

    @Column(name = "excess_shortfall_salary", precision = 12, scale = 2)
    private BigDecimal excessOrShortfallSalary = BigDecimal.ZERO;

    public Payslip() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getPayslipNumber() { return payslipNumber; }
    public void setPayslipNumber(String payslipNumber) { this.payslipNumber = payslipNumber; }

    public Integer getSalaryMonth() { return salaryMonth; }
    public void setSalaryMonth(Integer salaryMonth) { this.salaryMonth = salaryMonth; }

    public Integer getSalaryYear() { return salaryYear; }
    public void setSalaryYear(Integer salaryYear) { this.salaryYear = salaryYear; }

    public BigDecimal getAgreedMonthlySalary() { return agreedMonthlySalary; }
    public void setAgreedMonthlySalary(BigDecimal agreedMonthlySalary) { this.agreedMonthlySalary = agreedMonthlySalary; }

    public Integer getTotalWorkingDays() { return totalWorkingDays; }
    public void setTotalWorkingDays(Integer totalWorkingDays) { this.totalWorkingDays = totalWorkingDays; }

    public Integer getPresentDays() { return presentDays; }
    public void setPresentDays(Integer presentDays) { this.presentDays = presentDays; }

    public Integer getAbsentDays() { return absentDays; }
    public void setAbsentDays(Integer absentDays) { this.absentDays = absentDays; }

    public Integer getLeaveDays() { return leaveDays; }
    public void setLeaveDays(Integer leaveDays) { this.leaveDays = leaveDays; }

    public Integer getLateMinutes() { return lateMinutes; }
    public void setLateMinutes(Integer lateMinutes) { this.lateMinutes = lateMinutes; }

    public BigDecimal getTotalWorkingHours() { return totalWorkingHours; }
    public void setTotalWorkingHours(BigDecimal totalWorkingHours) { this.totalWorkingHours = totalWorkingHours; }

    public BigDecimal getGrossEarnings() { return grossEarnings; }
    public void setGrossEarnings(BigDecimal grossEarnings) { this.grossEarnings = grossEarnings; }

    public BigDecimal getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

    public BigDecimal getNetPayableSalary() { return netPayableSalary; }
    public void setNetPayableSalary(BigDecimal netPayableSalary) { this.netPayableSalary = netPayableSalary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public Integer getActualWorkingMinutes() { return actualWorkingMinutes; }
    public void setActualWorkingMinutes(Integer actualWorkingMinutes) { this.actualWorkingMinutes = actualWorkingMinutes; }

    public Integer getNormalWorkingMinutes() { return normalWorkingMinutes; }
    public void setNormalWorkingMinutes(Integer normalWorkingMinutes) { this.normalWorkingMinutes = normalWorkingMinutes; }

    public Integer getDifferenceMinutes() { return differenceMinutes; }
    public void setDifferenceMinutes(Integer differenceMinutes) { this.differenceMinutes = differenceMinutes; }

    public BigDecimal getSalaryPerMinute() { return salaryPerMinute; }
    public void setSalaryPerMinute(BigDecimal salaryPerMinute) { this.salaryPerMinute = salaryPerMinute; }

    public BigDecimal getExcessOrShortfallSalary() { return excessOrShortfallSalary; }
    public void setExcessOrShortfallSalary(BigDecimal excessOrShortfallSalary) { this.excessOrShortfallSalary = excessOrShortfallSalary; }
}
