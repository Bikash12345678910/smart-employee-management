package com.company.employee_management.repository;

import com.company.employee_management.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> findByYearOrderByHolidayDateAsc(Integer year);
    List<Holiday> findAllByOrderByHolidayDateAsc();
    Optional<Holiday> findByHolidayDate(LocalDate date);
    boolean existsByHolidayDate(LocalDate date);
}
