package com.company.employee_management.repository;

import com.company.employee_management.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    List<Attendance> findByAttendanceDate(LocalDate date);

    List<Attendance> findByEmployeeIdOrderByAttendanceDateDesc(Long employeeId);

    List<Attendance> findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(Long employeeId, LocalDate start, LocalDate end);

    List<Attendance> findByAttendanceDateBetweenOrderByAttendanceDateAsc(LocalDate start, LocalDate end);

    long countByAttendanceDateAndAttendanceStatus(LocalDate date, String status);

    long countByEmployeeIdAndAttendanceDateBetweenAndAttendanceStatus(Long employeeId, LocalDate start, LocalDate end, String status);

    @Query("SELECT a FROM Attendance a WHERE " +
           "(:date IS NULL OR a.attendanceDate = :date) AND " +
           "(:departmentId IS NULL OR a.employee.department.id = :departmentId) AND " +
           "(:status IS NULL OR a.attendanceStatus = :status) AND " +
           "(:search IS NULL OR LOWER(a.employee.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(a.employee.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(a.employee.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Attendance> findWithFilters(@Param("date") LocalDate date,
                                     @Param("departmentId") Long departmentId,
                                     @Param("status") String status,
                                     @Param("search") String search,
                                     Pageable pageable);

    @Query("SELECT a.attendanceStatus, COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date GROUP BY a.attendanceStatus")
    List<Object[]> getTodayAttendanceSummary(@Param("date") LocalDate date);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM Attendance a WHERE a.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Long employeeId);
}
