package com.company.employee_management.service;

import com.company.employee_management.dto.HolidayDTO;
import com.company.employee_management.entity.Holiday;
import com.company.employee_management.exception.BadRequestException;
import com.company.employee_management.exception.ResourceNotFoundException;
import com.company.employee_management.repository.HolidayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final AuditLogService auditLogService;

    public HolidayService(HolidayRepository holidayRepository, AuditLogService auditLogService) {
        this.holidayRepository = holidayRepository;
        this.auditLogService = auditLogService;
    }

    public List<HolidayDTO> getHolidays(Integer year) {
        List<Holiday> list;
        if (year != null) {
            list = holidayRepository.findByYearOrderByHolidayDateAsc(year);
        } else {
            list = holidayRepository.findAllByOrderByHolidayDateAsc();
        }
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public HolidayDTO addHoliday(HolidayDTO dto, String adminUsername) {
        if (dto.getHolidayDate() == null) {
            throw new BadRequestException("Holiday date is required");
        }
        if (holidayRepository.existsByHolidayDate(dto.getHolidayDate())) {
            throw new BadRequestException("A holiday already exists on " + dto.getHolidayDate());
        }

        Holiday h = new Holiday();
        h.setHolidayName(dto.getHolidayName());
        h.setHolidayDate(dto.getHolidayDate());
        h.setHolidayDay(dto.getHolidayDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        h.setDescription(dto.getDescription());
        h.setYear(dto.getHolidayDate().getYear());
        h.setIsActive(true);

        h = holidayRepository.save(h);

        auditLogService.logAction(adminUsername, "ADD_HOLIDAY", "HOLIDAY", h.getId().toString(),
                "Added holiday: " + h.getHolidayName() + " on " + h.getHolidayDate());

        return mapToDTO(h);
    }

    @Transactional
    public HolidayDTO updateHoliday(Long id, HolidayDTO dto, String adminUsername) {
        Holiday h = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with id: " + id));

        if (dto.getHolidayName() != null) h.setHolidayName(dto.getHolidayName());
        if (dto.getHolidayDate() != null) {
            h.setHolidayDate(dto.getHolidayDate());
            h.setHolidayDay(dto.getHolidayDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            h.setYear(dto.getHolidayDate().getYear());
        }
        if (dto.getDescription() != null) h.setDescription(dto.getDescription());
        if (dto.getIsActive() != null) h.setIsActive(dto.getIsActive());

        h = holidayRepository.save(h);

        auditLogService.logAction(adminUsername, "UPDATE_HOLIDAY", "HOLIDAY", id.toString(),
                "Updated holiday: " + h.getHolidayName());

        return mapToDTO(h);
    }

    @Transactional
    public void deleteHoliday(Long id, String adminUsername) {
        Holiday h = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with id: " + id));

        holidayRepository.delete(h);

        auditLogService.logAction(adminUsername, "DELETE_HOLIDAY", "HOLIDAY", id.toString(),
                "Deleted holiday: " + h.getHolidayName());
    }

    public String exportHolidaysCsv(Integer year) {
        List<HolidayDTO> list = getHolidays(year);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Date,Day,Holiday Name,Description,Year");
        for (HolidayDTO h : list) {
            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%d\"%n",
                    h.getHolidayDate(),
                    h.getHolidayDay(),
                    h.getHolidayName(),
                    h.getDescription() != null ? h.getDescription() : "",
                    h.getYear()
            );
        }
        return sw.toString();
    }

    public HolidayDTO mapToDTO(Holiday h) {
        if (h == null) return null;
        HolidayDTO dto = new HolidayDTO();
        dto.setId(h.getId());
        dto.setHolidayName(h.getHolidayName());
        dto.setHolidayDate(h.getHolidayDate());
        dto.setHolidayDay(h.getHolidayDay());
        dto.setDescription(h.getDescription());
        dto.setYear(h.getYear());
        dto.setIsActive(h.getIsActive());
        return dto;
    }
}
