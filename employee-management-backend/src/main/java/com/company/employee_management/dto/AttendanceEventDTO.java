package com.company.employee_management.dto;

import java.time.LocalDateTime;

public class AttendanceEventDTO {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String eventType; // CHECK_IN, CHECK_OUT
    private LocalDateTime eventTime;
    private String punchMethod;
    private Long deviceId;
    private String deviceName;

    public AttendanceEventDTO() {}

    public AttendanceEventDTO(Long id, Long employeeId, String employeeCode, String employeeName,
                              String eventType, LocalDateTime eventTime, String punchMethod,
                              Long deviceId, String deviceName) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.punchMethod = punchMethod;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }

    public String getPunchMethod() { return punchMethod; }
    public void setPunchMethod(String punchMethod) { this.punchMethod = punchMethod; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
}
