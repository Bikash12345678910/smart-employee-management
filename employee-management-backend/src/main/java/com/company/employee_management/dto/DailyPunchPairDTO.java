package com.company.employee_management.dto;

import java.time.LocalTime;

public class DailyPunchPairDTO {
    private int sequenceNumber;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private int workingMinutes;
    private String checkInMethod;
    private String checkOutMethod;
    private String status; // COMPLETED, IN_PROGRESS

    public DailyPunchPairDTO() {}

    public DailyPunchPairDTO(int sequenceNumber, LocalTime checkInTime, LocalTime checkOutTime, int workingMinutes, String checkInMethod, String checkOutMethod, String status) {
        this.sequenceNumber = sequenceNumber;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.workingMinutes = workingMinutes;
        this.checkInMethod = checkInMethod;
        this.checkOutMethod = checkOutMethod;
        this.status = status;
    }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public int getWorkingMinutes() { return workingMinutes; }
    public void setWorkingMinutes(int workingMinutes) { this.workingMinutes = workingMinutes; }

    public String getCheckInMethod() { return checkInMethod; }
    public void setCheckInMethod(String checkInMethod) { this.checkInMethod = checkInMethod; }

    public String getCheckOutMethod() { return checkOutMethod; }
    public void setCheckOutMethod(String checkOutMethod) { this.checkOutMethod = checkOutMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
