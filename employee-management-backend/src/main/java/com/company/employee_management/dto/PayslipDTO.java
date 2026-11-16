package com.company.employee_management.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PayslipDTO {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private String designationTitle;
    private String bankAccountNumberMasked;
    private String bankIfscCode;
    private String panNumberMasked;

    private String payslipNumber;
    private Integer salaryMonth;
    private Integer salaryYear;
    private String monthName;
    private BigDecimal agreedMonthlySalary;

    private Integer totalWorkingDays;
    private Integer presentDays;
    private Integer absentDays;
    private Integer leaveDays;
    private Integer lateMinutes;
    private BigDecimal totalWorkingHours;

    // Breakdown components
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal specialAllowance;
    private BigDecimal conveyance;
    private BigDecimal grossEarnings;

    private BigDecimal providentFund;
    private BigDecimal professionalTax;
    private BigDecimal leaveDeduction;
    private BigDecimal lateDeduction;
    private BigDecimal totalDeductions;

    private BigDecimal netPayableSalary;
    private String netSalaryInWords;

    private String status; // DRAFT, FINALIZED, PAID
    private LocalDateTime generatedAt;
    private LocalDate paymentDate;

    // Minute-based calculation fields
    private Integer actualWorkingMinutes;
    private Integer normalWorkingMinutes;
    private Integer differenceMinutes;
    private BigDecimal salaryPerMinute;
    private BigDecimal excessWorkingSalary;
    private BigDecimal shortfallSalary;
    private BigDecimal excessOrShortfallSalary;
    private BigDecimal totalSalaryAdjustment;
    private BigDecimal actualPayableSalary;

    public PayslipDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDesignationTitle() { return designationTitle; }
    public void setDesignationTitle(String designationTitle) { this.designationTitle = designationTitle; }

    public String getBankAccountNumberMasked() { return bankAccountNumberMasked; }
    public void setBankAccountNumberMasked(String bankAccountNumberMasked) { this.bankAccountNumberMasked = bankAccountNumberMasked; }

    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String bankIfscCode) { this.bankIfscCode = bankIfscCode; }

    public String getPanNumberMasked() { return panNumberMasked; }
    public void setPanNumberMasked(String panNumberMasked) { this.panNumberMasked = panNumberMasked; }

    public String getPayslipNumber() { return payslipNumber; }
    public void setPayslipNumber(String payslipNumber) { this.payslipNumber = payslipNumber; }

    public Integer getSalaryMonth() { return salaryMonth; }
    public void setSalaryMonth(Integer salaryMonth) { this.salaryMonth = salaryMonth; }

    public Integer getSalaryYear() { return salaryYear; }
    public void setSalaryYear(Integer salaryYear) { this.salaryYear = salaryYear; }

    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }

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

    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }

    public BigDecimal getHra() { return hra; }
    public void setHra(BigDecimal hra) { this.hra = hra; }

    public BigDecimal getSpecialAllowance() { return specialAllowance; }
    public void setSpecialAllowance(BigDecimal specialAllowance) { this.specialAllowance = specialAllowance; }

    public BigDecimal getConveyance() { return conveyance; }
    public void setConveyance(BigDecimal conveyance) { this.conveyance = conveyance; }

    public BigDecimal getGrossEarnings() { return grossEarnings; }
    public void setGrossEarnings(BigDecimal grossEarnings) { this.grossEarnings = grossEarnings; }

    public BigDecimal getProvidentFund() { return providentFund; }
    public void setProvidentFund(BigDecimal providentFund) { this.providentFund = providentFund; }

    public BigDecimal getProfessionalTax() { return professionalTax; }
    public void setProfessionalTax(BigDecimal professionalTax) { this.professionalTax = professionalTax; }

    public BigDecimal getLeaveDeduction() { return leaveDeduction; }
    public void setLeaveDeduction(BigDecimal leaveDeduction) { this.leaveDeduction = leaveDeduction; }

    public BigDecimal getLateDeduction() { return lateDeduction; }
    public void setLateDeduction(BigDecimal lateDeduction) { this.lateDeduction = lateDeduction; }

    public BigDecimal getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

    public BigDecimal getNetPayableSalary() { return netPayableSalary; }
    public void setNetPayableSalary(BigDecimal netPayableSalary) { this.netPayableSalary = netPayableSalary; }

    public String getNetSalaryInWords() { return netSalaryInWords; }
    public void setNetSalaryInWords(String netSalaryInWords) { this.netSalaryInWords = netSalaryInWords; }

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

    public BigDecimal getExcessWorkingSalary() { return excessWorkingSalary; }
    public void setExcessWorkingSalary(BigDecimal excessWorkingSalary) { this.excessWorkingSalary = excessWorkingSalary; }

    public BigDecimal getShortfallSalary() { return shortfallSalary; }
    public void setShortfallSalary(BigDecimal shortfallSalary) { this.shortfallSalary = shortfallSalary; }

    public BigDecimal getExcessOrShortfallSalary() { return excessOrShortfallSalary != null ? excessOrShortfallSalary : totalSalaryAdjustment; }
    public void setExcessOrShortfallSalary(BigDecimal excessOrShortfallSalary) { this.excessOrShortfallSalary = excessOrShortfallSalary; }

    public BigDecimal getTotalSalaryAdjustment() { return totalSalaryAdjustment; }
    public void setTotalSalaryAdjustment(BigDecimal totalSalaryAdjustment) { this.totalSalaryAdjustment = totalSalaryAdjustment; }

    public BigDecimal getActualPayableSalary() { return actualPayableSalary; }
    public void setActualPayableSalary(BigDecimal actualPayableSalary) { this.actualPayableSalary = actualPayableSalary; }
}
