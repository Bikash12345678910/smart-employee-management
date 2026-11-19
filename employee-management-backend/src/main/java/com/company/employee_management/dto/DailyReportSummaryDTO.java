package com.company.employee_management.dto;

import java.util.List;

public class DailyReportSummaryDTO {
    private String date;
    private int totalEmployees;
    private int presentCount;
    private int absentCount;
    private int lateCount;
    private int checkedOutCount;
    private List<DailyReportRow> records;

    public static class DailyReportRow {
        private String employeeCode;
        private String employeeName;
        private String status; // PRESENT, LATE, ABSENT
        private String checkInTime;
        private String checkOutTime;
        private Long workingMinutes;

        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCheckInTime() { return checkInTime; }
        public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

        public String getCheckOutTime() { return checkOutTime; }
        public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

        public Long getWorkingMinutes() { return workingMinutes; }
        public void setWorkingMinutes(Long workingMinutes) { this.workingMinutes = workingMinutes; }
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; }

    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

    public int getLateCount() { return lateCount; }
    public void setLateCount(int lateCount) { this.lateCount = lateCount; }

    public int getCheckedOutCount() { return checkedOutCount; }
    public void setCheckedOutCount(int checkedOutCount) { this.checkedOutCount = checkedOutCount; }

    public List<DailyReportRow> getRecords() { return records; }
    public void setRecords(List<DailyReportRow> records) { this.records = records; }
}
