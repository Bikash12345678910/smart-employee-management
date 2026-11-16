package com.company.employee_management.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AdminDashboardStatsDTO {

    // 6 Main Animated Stat Cards
    private Long totalEmployees = 0L;
    private Long onLeaveToday = 0L;
    private Long presentToday = 0L;
    private Long lateToday = 0L;
    private Long absentToday = 0L;
    private Long halfDayToday = 0L;

    // Additional Sections
    private Double attendancePercentage = 0.0;
    private BigDecimal monthlySalaryPayout = BigDecimal.ZERO;
    private Integer totalDepartments = 0;
    private Integer onlineDevices = 0;

    private List<Map<String, Object>> monthlyAttendanceTrend;
    private List<Map<String, Object>> departmentDistribution;
    private List<AttendanceDTO> recentAttendanceActivity;
    private List<EmployeeDTO> recentRegistrations;

    public AdminDashboardStatsDTO() {}

    public Long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(Long totalEmployees) { this.totalEmployees = totalEmployees; }

    public Long getOnLeaveToday() { return onLeaveToday; }
    public void setOnLeaveToday(Long onLeaveToday) { this.onLeaveToday = onLeaveToday; }

    public Long getPresentToday() { return presentToday; }
    public void setPresentToday(Long presentToday) { this.presentToday = presentToday; }

    public Long getLateToday() { return lateToday; }
    public void setLateToday(Long lateToday) { this.lateToday = lateToday; }

    public Long getAbsentToday() { return absentToday; }
    public void setAbsentToday(Long absentToday) { this.absentToday = absentToday; }

    public Long getHalfDayToday() { return halfDayToday; }
    public void setHalfDayToday(Long halfDayToday) { this.halfDayToday = halfDayToday; }

    public Double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(Double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public BigDecimal getMonthlySalaryPayout() { return monthlySalaryPayout; }
    public void setMonthlySalaryPayout(BigDecimal monthlySalaryPayout) { this.monthlySalaryPayout = monthlySalaryPayout; }

    public Integer getTotalDepartments() { return totalDepartments; }
    public void setTotalDepartments(Integer totalDepartments) { this.totalDepartments = totalDepartments; }

    public Integer getOnlineDevices() { return onlineDevices; }
    public void setOnlineDevices(Integer onlineDevices) { this.onlineDevices = onlineDevices; }

    public List<Map<String, Object>> getMonthlyAttendanceTrend() { return monthlyAttendanceTrend; }
    public void setMonthlyAttendanceTrend(List<Map<String, Object>> monthlyAttendanceTrend) { this.monthlyAttendanceTrend = monthlyAttendanceTrend; }

    public List<Map<String, Object>> getDepartmentDistribution() { return departmentDistribution; }
    public void setDepartmentDistribution(List<Map<String, Object>> departmentDistribution) { this.departmentDistribution = departmentDistribution; }

    public List<AttendanceDTO> getRecentAttendanceActivity() { return recentAttendanceActivity; }
    public void setRecentAttendanceActivity(List<AttendanceDTO> recentAttendanceActivity) { this.recentAttendanceActivity = recentAttendanceActivity; }

    public List<EmployeeDTO> getRecentRegistrations() { return recentRegistrations; }
    public void setRecentRegistrations(List<EmployeeDTO> recentRegistrations) { this.recentRegistrations = recentRegistrations; }
}
