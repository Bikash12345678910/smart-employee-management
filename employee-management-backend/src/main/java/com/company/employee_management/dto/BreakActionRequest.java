package com.company.employee_management.dto;

import jakarta.validation.constraints.NotBlank;

public class BreakActionRequest {

    private Long employeeId;

    @NotBlank(message = "Action is required (START or END)")
    private String action; // START, END

    private String reason = "TEA/LUNCH";

    public BreakActionRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
