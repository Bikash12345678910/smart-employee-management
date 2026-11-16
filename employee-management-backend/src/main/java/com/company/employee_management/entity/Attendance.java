package com.company.employee_management.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "attendance_date"})
})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Column(name = "working_hours")
    private Integer workingHours = 0;

    @Column(name = "working_minutes")
    private Integer workingMinutes = 0;

    @Column(name = "attendance_status", nullable = false, length = 30)
    private String attendanceStatus; // PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE, HOLIDAY

    @Column(name = "attendance_via", length = 30)
    private String attendanceVia = "MANUAL"; // FACE_RECOGNITION, FINGERPRINT, PASSWORD, MANUAL

    @Column(name = "break_count")
    private Integer breakCount = 0;

    @Column(name = "total_break_minutes")
    private Integer totalBreakMinutes = 0;

    @Column(name = "late_minutes")
    private Integer lateMinutes = 0;

    @Column(name = "early_departure_minutes")
    private Integer earlyDepartureMinutes = 0;

    @Column(name = "is_manual_correction")
    private Boolean isManualCorrection = false;

    @Column(name = "correction_reason", columnDefinition = "TEXT")
    private String correctionReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Attendance() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
