package com.company.employee_management.dto;

import java.math.BigDecimal;

public class SalaryCalculationDTO {

    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private BigDecimal agreedSalary;
    private Integer actualWorkingMinutes;
    private Integer normalWorkingMinutes;
    private Integer differenceMinutes;
    private BigDecimal salaryPerMinute;
    private BigDecimal excessOrShortfallSalary;
    private BigDecimal actualPayableSalary;
    private Long payslipId;
    private String status;

    public SalaryCalculationDTO() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public BigDecimal getAgreedSalary() { return agreedSalary; }
    public void setAgreedSalary(BigDecimal agreedSalary) { this.agreedSalary = agreedSalary; }

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

    public BigDecimal getActualPayableSalary() { return actualPayableSalary; }
    public void setActualPayableSalary(BigDecimal actualPayableSalary) { this.actualPayableSalary = actualPayableSalary; }

    public Long getPayslipId() { return payslipId; }
    public void setPayslipId(Long payslipId) { this.payslipId = payslipId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
