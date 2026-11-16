package com.company.employee_management.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class GeneratePayslipRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Salary Month is required")
    private Integer month;

    @NotNull(message = "Salary Year is required")
    private Integer year;

    // Optional override allowances or deductions if reviewed by admin
    private BigDecimal bonusOrIncentive;
    private BigDecimal customDeduction;

    public GeneratePayslipRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public BigDecimal getBonusOrIncentive() { return bonusOrIncentive; }
    public void setBonusOrIncentive(BigDecimal bonusOrIncentive) { this.bonusOrIncentive = bonusOrIncentive; }

    public BigDecimal getCustomDeduction() { return customDeduction; }
    public void setCustomDeduction(BigDecimal customDeduction) { this.customDeduction = customDeduction; }
}
