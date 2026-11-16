package com.company.employee_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class HolidayDTO {

    private Long id;

    @NotBlank(message = "Holiday name is required")
    private String holidayName;

    @NotNull(message = "Holiday date is required")
    private LocalDate holidayDate;

    private String holidayDay;
    private String description;
    private Integer year;
    private Boolean isActive = true;

    public HolidayDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHolidayName() { return holidayName; }
    public void setHolidayName(String holidayName) { this.holidayName = holidayName; }

    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }

    public String getHolidayDay() { return holidayDay; }
    public void setHolidayDay(String holidayDay) { this.holidayDay = holidayDay; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
