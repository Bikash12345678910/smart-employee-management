package com.company.employee_management.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyPunchSummaryDTO {
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate date;
    private List<DailyPunchPairDTO> punchPairs = new ArrayList<>();
    private List<AttendanceEventDTO> rawEvents = new ArrayList<>();
    private int totalWorkingMinutes;
    private String currentStatus; // CHECKED_IN, CHECKED_OUT, NOT_REPORTED

    public DailyPunchSummaryDTO() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<DailyPunchPairDTO> getPunchPairs() { return punchPairs; }
    public void setPunchPairs(List<DailyPunchPairDTO> punchPairs) { this.punchPairs = punchPairs; }

    public List<AttendanceEventDTO> getRawEvents() { return rawEvents; }
    public void setRawEvents(List<AttendanceEventDTO> rawEvents) { this.rawEvents = rawEvents; }

    public int getTotalWorkingMinutes() { return totalWorkingMinutes; }
    public void setTotalWorkingMinutes(int totalWorkingMinutes) { this.totalWorkingMinutes = totalWorkingMinutes; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
}
