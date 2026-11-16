package com.company.employee_management.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class EmployeeDashboardStatsDTO {

    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private String departmentName;
    private String designationTitle;
    private String profilePhotoUrl;

    // Today's status
    private LocalDate todayDate = LocalDate.now();
    private String todayStatus = "ABSENT";
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Integer todayWorkingHours = 0;
    private Integer todayWorkingMinutes = 0;
    private Boolean isOnBreak = false;
    private LocalTime currentBreakStart;

    // Monthly Stat Cards
    private Long presentDays = 0L;
    private Long absentDays = 0L;
    private Long leaveDays = 0L;
    private Double totalWorkingHoursMonth = 0.0;
    private Double attendancePercentage = 0.0;

    // Recent punches & Upcoming Holidays
    private List<AttendanceDTO> recentAttendance;
    private List<HolidayDTO> upcomingHolidays;

    public EmployeeDashboardStatsDTO() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDesignationTitle() { return designationTitle; }
    public void setDesignationTitle(String designationTitle) { this.designationTitle = designationTitle; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public LocalDate getTodayDate() { return todayDate; }
    public void setTodayDate(LocalDate todayDate) { this.todayDate = todayDate; }

    public String getTodayStatus() { return todayStatus; }
    public void setTodayStatus(String todayStatus) { this.todayStatus = todayStatus; }

    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public Integer getTodayWorkingHours() { return todayWorkingHours; }
    public void setTodayWorkingHours(Integer todayWorkingHours) { this.todayWorkingHours = todayWorkingHours; }

    public Integer getTodayWorkingMinutes() { return todayWorkingMinutes; }
    public void setTodayWorkingMinutes(Integer todayWorkingMinutes) { this.todayWorkingMinutes = todayWorkingMinutes; }

    public Boolean getIsOnBreak() { return isOnBreak; }
    public void setIsOnBreak(Boolean isOnBreak) { this.isOnBreak = isOnBreak; }

    public LocalTime getCurrentBreakStart() { return currentBreakStart; }
    public void setCurrentBreakStart(LocalTime currentBreakStart) { this.currentBreakStart = currentBreakStart; }

    public Long getPresentDays() { return presentDays; }
    public void setPresentDays(Long presentDays) { this.presentDays = presentDays; }

    public Long getAbsentDays() { return absentDays; }
    public void setAbsentDays(Long absentDays) { this.absentDays = absentDays; }

    public Long getLeaveDays() { return leaveDays; }
    public void setLeaveDays(Long leaveDays) { this.leaveDays = leaveDays; }

    public Double getTotalWorkingHoursMonth() { return totalWorkingHoursMonth; }
    public void setTotalWorkingHoursMonth(Double totalWorkingHoursMonth) { this.totalWorkingHoursMonth = totalWorkingHoursMonth; }

    public Double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(Double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public List<AttendanceDTO> getRecentAttendance() { return recentAttendance; }
    public void setRecentAttendance(List<AttendanceDTO> recentAttendance) { this.recentAttendance = recentAttendance; }

    public List<HolidayDTO> getUpcomingHolidays() { return upcomingHolidays; }
    public void setUpcomingHolidays(List<HolidayDTO> upcomingHolidays) { this.upcomingHolidays = upcomingHolidays; }
}
