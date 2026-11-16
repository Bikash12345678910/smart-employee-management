package com.company.employee_management.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class BreakDTO {

    private Long id;
    private Long attendanceId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate breakDate;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private Integer durationMinutes;
    private String breakReason;

    public BreakDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public LocalDate getBreakDate() { return breakDate; }
    public void setBreakDate(LocalDate breakDate) { this.breakDate = breakDate; }

    public LocalTime getBreakStartTime() { return breakStartTime; }
    public void setBreakStartTime(LocalTime breakStartTime) { this.breakStartTime = breakStartTime; }

    public LocalTime getBreakEndTime() { return breakEndTime; }
    public void setBreakEndTime(LocalTime breakEndTime) { this.breakEndTime = breakEndTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getBreakReason() { return breakReason; }
    public void setBreakReason(String breakReason) { this.breakReason = breakReason; }
}
