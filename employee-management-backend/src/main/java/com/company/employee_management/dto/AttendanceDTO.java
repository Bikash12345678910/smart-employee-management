package com.company.employee_management.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDTO {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private String designationTitle;
    private String profilePhotoUrl;

    private LocalDate attendanceDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Integer workingHours;
    private Integer workingMinutes;
    private Integer totalWorkingMinutes;
    private String attendanceStatus; // PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE, HOLIDAY
    private String attendanceVia; // FACE_RECOGNITION, FINGERPRINT, PASSWORD, MANUAL
    private Integer breakCount;
    private Integer totalBreakMinutes;
    private Integer lateMinutes;
    private Integer earlyDepartureMinutes;
    private Boolean isManualCorrection;
    private String correctionReason;
    private List<AttendanceEventDTO> punches = new ArrayList<>();

    public AttendanceDTO() {}

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

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public Integer getWorkingHours() { return workingHours; }
    public void setWorkingHours(Integer workingHours) { this.workingHours = workingHours; }

    public Integer getWorkingMinutes() { return workingMinutes; }
    public void setWorkingMinutes(Integer workingMinutes) { this.workingMinutes = workingMinutes; }

    public Integer getTotalWorkingMinutes() {
        if (totalWorkingMinutes != null) return totalWorkingMinutes;
        int h = workingHours != null ? workingHours : 0;
        int m = workingMinutes != null ? workingMinutes : 0;
        return h * 60 + m;
    }
    public void setTotalWorkingMinutes(Integer totalWorkingMinutes) { this.totalWorkingMinutes = totalWorkingMinutes; }

    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    public String getAttendanceVia() { return attendanceVia; }
    public void setAttendanceVia(String attendanceVia) { this.attendanceVia = attendanceVia; }

    public Integer getBreakCount() { return breakCount; }
    public void setBreakCount(Integer breakCount) { this.breakCount = breakCount; }

    public Integer getTotalBreakMinutes() { return totalBreakMinutes; }
    public void setTotalBreakMinutes(Integer totalBreakMinutes) { this.totalBreakMinutes = totalBreakMinutes; }

    public Integer getLateMinutes() { return lateMinutes; }
    public void setLateMinutes(Integer lateMinutes) { this.lateMinutes = lateMinutes; }

    public Integer getEarlyDepartureMinutes() { return earlyDepartureMinutes; }
    public void setEarlyDepartureMinutes(Integer earlyDepartureMinutes) { this.earlyDepartureMinutes = earlyDepartureMinutes; }

    public Boolean getIsManualCorrection() { return isManualCorrection; }
    public void setIsManualCorrection(Boolean isManualCorrection) { this.isManualCorrection = isManualCorrection; }

    public String getCorrectionReason() { return correctionReason; }
    public void setCorrectionReason(String correctionReason) { this.correctionReason = correctionReason; }

    public List<AttendanceEventDTO> getPunches() { return punches; }
    public void setPunches(List<AttendanceEventDTO> punches) { this.punches = punches; }
}
