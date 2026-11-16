package com.company.employee_management.repository;

import com.company.employee_management.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByPhoneNumber(String phoneNumber);
    Optional<Employee> findByUserId(Long userId);

    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    long countByIsActive(Boolean isActive);
    long countByEmploymentStatus(String employmentStatus);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
           "(:status IS NULL OR e.employmentStatus = :status)")
    Page<Employee> findWithFilters(@Param("search") String search,
                                   @Param("departmentId") Long departmentId,
                                   @Param("status") String status,
                                   Pageable pageable);

    @Query("SELECT MAX(CAST(SUBSTRING(e.employeeCode, 4) AS integer)) FROM Employee e WHERE e.employeeCode LIKE 'EMP%'")
    Integer findMaxEmployeeCodeSuffix();
}
