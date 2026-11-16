package com.company.employee_management.service;

import com.company.employee_management.dto.CompanySettingDTO;
import com.company.employee_management.entity.CompanySetting;
import com.company.employee_management.repository.CompanySettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanySettingsService {

    private final CompanySettingRepository companySettingRepository;
    private final AuditLogService auditLogService;

    public CompanySettingsService(CompanySettingRepository companySettingRepository, AuditLogService auditLogService) {
        this.companySettingRepository = companySettingRepository;
        this.auditLogService = auditLogService;
    }

    public CompanySettingDTO getSettings() {
        CompanySetting setting = companySettingRepository.findById(1L).orElseGet(() -> {
            CompanySetting s = new CompanySetting();
            return companySettingRepository.save(s);
        });
        return mapToDTO(setting);
    }

    @Transactional
    public CompanySettingDTO updateSettings(CompanySettingDTO dto, String adminUsername) {
        CompanySetting s = companySettingRepository.findById(1L).orElseGet(CompanySetting::new);

        if (dto.getCompanyName() != null) s.setCompanyName(dto.getCompanyName());
        if (dto.getCompanyEmail() != null) s.setCompanyEmail(dto.getCompanyEmail());
        if (dto.getContactNumber() != null) s.setContactNumber(dto.getContactNumber());
        if (dto.getCompanyAddress() != null) s.setCompanyAddress(dto.getCompanyAddress());
        if (dto.getCompanyLogoUrl() != null) s.setCompanyLogoUrl(dto.getCompanyLogoUrl());

        if (dto.getShiftStartTime() != null) s.setShiftStartTime(dto.getShiftStartTime());
        if (dto.getShiftEndTime() != null) s.setShiftEndTime(dto.getShiftEndTime());
        if (dto.getLateArrivalGraceMinutes() != null) s.setLateArrivalGraceMinutes(dto.getLateArrivalGraceMinutes());
        if (dto.getHalfDayThresholdHours() != null) s.setHalfDayThresholdHours(dto.getHalfDayThresholdHours());
        if (dto.getFullDayWorkingHours() != null) s.setFullDayWorkingHours(dto.getFullDayWorkingHours());
        if (dto.getWeeklyOffDays() != null) s.setWeeklyOffDays(dto.getWeeklyOffDays());

        if (dto.getEnableFaceAttendance() != null) s.setEnableFaceAttendance(dto.getEnableFaceAttendance());
        if (dto.getEnableFingerprintAttendance() != null) s.setEnableFingerprintAttendance(dto.getEnableFingerprintAttendance());
        if (dto.getEnablePasswordAttendance() != null) s.setEnablePasswordAttendance(dto.getEnablePasswordAttendance());
        if (dto.getAllowManualAttendance() != null) s.setAllowManualAttendance(dto.getAllowManualAttendance());
        if (dto.getMaxBreaksAllowed() != null) s.setMaxBreaksAllowed(dto.getMaxBreaksAllowed());
        if (dto.getMaxBreakMinutes() != null) s.setMaxBreakMinutes(dto.getMaxBreakMinutes());

        if (dto.getStandardWorkingDays() != null) s.setStandardWorkingDays(dto.getStandardWorkingDays());
        if (dto.getLeaveDeductionRate() != null) s.setLeaveDeductionRate(dto.getLeaveDeductionRate());
        if (dto.getOvertimeRatePerHour() != null) s.setOvertimeRatePerHour(dto.getOvertimeRatePerHour());
        if (dto.getCurrencyCode() != null) s.setCurrencyCode(dto.getCurrencyCode());
        if (dto.getCurrencySymbol() != null) s.setCurrencySymbol(dto.getCurrencySymbol());

        s = companySettingRepository.save(s);

        auditLogService.logAction(adminUsername, "UPDATE_SETTINGS", "SETTINGS", "1", "Updated company shift & attendance settings");

        return mapToDTO(s);
    }

    public CompanySettingDTO mapToDTO(CompanySetting s) {
        if (s == null) return null;
        CompanySettingDTO dto = new CompanySettingDTO();
        dto.setId(s.getId());
        dto.setCompanyName(s.getCompanyName());
        dto.setCompanyEmail(s.getCompanyEmail());
        dto.setContactNumber(s.getContactNumber());
        dto.setCompanyAddress(s.getCompanyAddress());
        dto.setCompanyLogoUrl(s.getCompanyLogoUrl());

        dto.setShiftStartTime(s.getShiftStartTime());
        dto.setShiftEndTime(s.getShiftEndTime());
        dto.setLateArrivalGraceMinutes(s.getLateArrivalGraceMinutes());
        dto.setHalfDayThresholdHours(s.getHalfDayThresholdHours());
        dto.setFullDayWorkingHours(s.getFullDayWorkingHours());
        dto.setWeeklyOffDays(s.getWeeklyOffDays());

        dto.setEnableFaceAttendance(s.getEnableFaceAttendance());
        dto.setEnableFingerprintAttendance(s.getEnableFingerprintAttendance());
        dto.setEnablePasswordAttendance(s.getEnablePasswordAttendance());
        dto.setAllowManualAttendance(s.getAllowManualAttendance());
        dto.setMaxBreaksAllowed(s.getMaxBreaksAllowed());
        dto.setMaxBreakMinutes(s.getMaxBreakMinutes());

        dto.setStandardWorkingDays(s.getStandardWorkingDays());
        dto.setLeaveDeductionRate(s.getLeaveDeductionRate());
        dto.setOvertimeRatePerHour(s.getOvertimeRatePerHour());
        dto.setCurrencyCode(s.getCurrencyCode() != null ? s.getCurrencyCode() : "INR");
        dto.setCurrencySymbol(s.getCurrencySymbol() != null ? s.getCurrencySymbol() : "₹");
        return dto;
    }
}
