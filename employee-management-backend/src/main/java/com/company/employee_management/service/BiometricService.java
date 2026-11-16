package com.company.employee_management.service;

import com.company.employee_management.dto.BiometricDeviceDTO;
import com.company.employee_management.dto.BiometricEnrollRequest;
import com.company.employee_management.entity.BiometricDevice;
import com.company.employee_management.entity.BiometricRegistration;
import com.company.employee_management.entity.Employee;
import com.company.employee_management.exception.BadRequestException;
import com.company.employee_management.exception.ResourceNotFoundException;
import com.company.employee_management.integration.biometric.BiometricDeviceGateway;
import com.company.employee_management.repository.BiometricDeviceRepository;
import com.company.employee_management.repository.BiometricRegistrationRepository;
import com.company.employee_management.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BiometricService {

    private final BiometricRegistrationRepository registrationRepository;
    private final BiometricDeviceRepository deviceRepository;
    private final EmployeeRepository employeeRepository;
    private final BiometricDeviceGateway gateway;
    private final AuditLogService auditLogService;

    public BiometricService(BiometricRegistrationRepository registrationRepository,
                            BiometricDeviceRepository deviceRepository,
                            EmployeeRepository employeeRepository,
                            BiometricDeviceGateway gateway,
                            AuditLogService auditLogService) {
        this.registrationRepository = registrationRepository;
        this.deviceRepository = deviceRepository;
        this.employeeRepository = employeeRepository;
        this.gateway = gateway;
        this.auditLogService = auditLogService;
    }

    public List<BiometricDeviceDTO> getAllDevices() {
        return deviceRepository.findAll().stream().map(this::mapDeviceToDTO).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> enrollBiometric(BiometricEnrollRequest req, String adminUsername) {
        Employee emp = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + req.getEmployeeId()));

        String type = req.getBiometricType().toUpperCase();
        if (!"FACE".equals(type) && !"FINGERPRINT".equals(type)) {
            throw new BadRequestException("Biometric type must be FACE or FINGERPRINT");
        }

        BiometricRegistration registration = registrationRepository.findByEmployeeIdAndBiometricType(emp.getId(), type)
                .orElse(new BiometricRegistration());

        registration.setEmployee(emp);
        registration.setBiometricType(type);

        if (req.getDeviceId() != null) {
            BiometricDevice device = deviceRepository.findById(req.getDeviceId())
                    .orElseThrow(() -> new BadRequestException("Biometric device not found"));
            registration.setDevice(device);
        }

        if ("FACE".equals(type)) {
            registration.setFaceSampleData(req.getRawSampleOrTemplate());
            registration.setTemplateHash(gateway.hashBiometricTemplate(req.getRawSampleOrTemplate(), emp.getEmployeeCode()));
        } else {
            registration.setTemplateHash(gateway.hashBiometricTemplate(req.getRawSampleOrTemplate() != null ? req.getRawSampleOrTemplate() : UUID.randomUUID().toString(), emp.getEmployeeCode()));
            registration.setFingerprintSlot(req.getSlotNumber() != null ? req.getSlotNumber() : emp.getId().intValue());
        }

        registration.setIsActive(true);
        registrationRepository.save(registration);

        auditLogService.logAction(adminUsername, "BIOMETRIC_ENROLL", "BIOMETRIC", emp.getId().toString(),
                "Enrolled " + type + " biometric template for employee " + emp.getEmployeeCode());

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("employeeId", emp.getId());
        res.put("employeeCode", emp.getEmployeeCode());
        res.put("biometricType", type);
        res.put("status", "ENROLLED");
        res.put("registeredAt", registration.getEnrolledAt());
        return res;
    }

    public Map<String, Object> getBiometricStatus(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        boolean face = registrationRepository.existsByEmployeeIdAndBiometricType(emp.getId(), "FACE");
        boolean fp = registrationRepository.existsByEmployeeIdAndBiometricType(emp.getId(), "FINGERPRINT");

        Map<String, Object> res = new HashMap<>();
        res.put("employeeId", emp.getId());
        res.put("employeeCode", emp.getEmployeeCode());
        res.put("employeeName", emp.getFullName());
        res.put("faceRegistered", face);
        res.put("fingerprintRegistered", fp);
        return res;
    }

    public boolean pingDevice(Long deviceId) {
        BiometricDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        return gateway.pingDevice(device);
    }

    public Map<String, Object> testDeviceConnection(Long deviceId) {
        BiometricDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));
        return gateway.testDeviceConnection(device);
    }

    @Transactional
    public BiometricDeviceDTO createDevice(BiometricDeviceDTO req, String adminUsername) {
        if (req.getDeviceName() == null || req.getDeviceName().trim().isEmpty()) {
            throw new BadRequestException("Device Name is required");
        }
        String serialNumber = req.getDeviceSerialNumber();
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            serialNumber = req.getDeviceUuid();
        }
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            serialNumber = "DEV-" + System.currentTimeMillis();
        }

        if (deviceRepository.findByDeviceSerialNumber(serialNumber).isPresent()) {
            throw new BadRequestException("Device with Serial Number / UUID '" + serialNumber + "' already exists");
        }

        BiometricDevice device = new BiometricDevice();
        device.setDeviceName(req.getDeviceName().trim());
        device.setDeviceType(req.getDeviceType() != null ? req.getDeviceType().toUpperCase() : "BIOMETRIC");
        device.setDeviceModel(req.getDeviceModel() != null ? req.getDeviceModel().trim() : "Standard Model");
        device.setDeviceSerialNumber(serialNumber.trim());
        device.setDeviceUuid(req.getDeviceUuid() != null ? req.getDeviceUuid().trim() : serialNumber.trim());
        device.setIpAddress(req.getIpAddress() != null && !req.getIpAddress().trim().isEmpty() ? req.getIpAddress().trim() : "127.0.0.1");
        device.setPort(req.getPort() != null ? req.getPort() : ("MOBILE".equalsIgnoreCase(req.getDeviceType()) ? 8080 : 4370));
        device.setLocation(req.getLocation() != null ? req.getLocation().trim() : "Office");
        device.setStatus(req.getStatus() != null ? req.getStatus() : "DISCONNECTED");
        device.setManufacturer(req.getManufacturer() != null ? req.getManufacturer().trim() : ("MOBILE".equalsIgnoreCase(req.getDeviceType()) ? "Samsung" : "ZKTeco"));
        device.setDevicePlatform(req.getDevicePlatform() != null ? req.getDevicePlatform().trim() : "Android");
        device.setAndroidVersion(req.getAndroidVersion() != null ? req.getAndroidVersion().trim() : "Android 14");
        device.setAppVersion(req.getAppVersion() != null ? req.getAppVersion().trim() : "v2.4.0-kiosk");
        device.setDepartmentName(req.getDepartmentName() != null ? req.getDepartmentName().trim() : "General");
        device.setConnectionType(req.getConnectionType() != null ? req.getConnectionType().trim() : "Wi-Fi");
        device.setCommunicationProtocol(req.getCommunicationProtocol() != null ? req.getCommunicationProtocol().trim() : "TCP/IP");
        device.setAssignedEmployeeId(req.getAssignedEmployeeId());
        device.setAssignedEmployeeName(req.getAssignedEmployeeName());
        device.setDeviceUsername(req.getDeviceUsername());
        device.setDevicePassword(req.getDevicePassword());

        device = deviceRepository.save(device);

        auditLogService.logAction(adminUsername, "CREATE_DEVICE", "BIOMETRIC_DEVICE", device.getId().toString(),
                "Registered new " + device.getDeviceType() + " device: " + device.getDeviceName());

        return mapDeviceToDTO(device);
    }

    @Transactional
    public BiometricDeviceDTO updateDevice(Long id, BiometricDeviceDTO req, String adminUsername) {
        BiometricDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        if (req.getDeviceName() != null && !req.getDeviceName().trim().isEmpty()) {
            device.setDeviceName(req.getDeviceName().trim());
        }
        if (req.getDeviceType() != null) device.setDeviceType(req.getDeviceType().toUpperCase());
        if (req.getDeviceModel() != null) device.setDeviceModel(req.getDeviceModel().trim());
        if (req.getDeviceSerialNumber() != null && !req.getDeviceSerialNumber().trim().isEmpty()) {
            device.setDeviceSerialNumber(req.getDeviceSerialNumber().trim());
        }
        if (req.getDeviceUuid() != null) device.setDeviceUuid(req.getDeviceUuid().trim());
        if (req.getIpAddress() != null) device.setIpAddress(req.getIpAddress().trim());
        if (req.getPort() != null) device.setPort(req.getPort());
        if (req.getLocation() != null) device.setLocation(req.getLocation().trim());
        if (req.getStatus() != null) device.setStatus(req.getStatus());
        if (req.getManufacturer() != null) device.setManufacturer(req.getManufacturer().trim());
        if (req.getDevicePlatform() != null) device.setDevicePlatform(req.getDevicePlatform().trim());
        if (req.getAndroidVersion() != null) device.setAndroidVersion(req.getAndroidVersion().trim());
        if (req.getAppVersion() != null) device.setAppVersion(req.getAppVersion().trim());
        if (req.getDepartmentName() != null) device.setDepartmentName(req.getDepartmentName().trim());
        if (req.getConnectionType() != null) device.setConnectionType(req.getConnectionType().trim());
        if (req.getCommunicationProtocol() != null) device.setCommunicationProtocol(req.getCommunicationProtocol().trim());
        if (req.getAssignedEmployeeId() != null) device.setAssignedEmployeeId(req.getAssignedEmployeeId());
        if (req.getAssignedEmployeeName() != null) device.setAssignedEmployeeName(req.getAssignedEmployeeName());
        if (req.getDeviceUsername() != null) device.setDeviceUsername(req.getDeviceUsername());
        if (req.getDevicePassword() != null) device.setDevicePassword(req.getDevicePassword());

        device = deviceRepository.save(device);

        auditLogService.logAction(adminUsername, "UPDATE_DEVICE", "BIOMETRIC_DEVICE", device.getId().toString(),
                "Updated " + device.getDeviceType() + " device: " + device.getDeviceName());

        return mapDeviceToDTO(device);
    }

    @Transactional
    public void deleteDevice(Long id, String adminUsername) {
        BiometricDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        // Disallow deleting the core demo devices if needed, or allow clean removal
        registrationRepository.findByDeviceId(id).forEach(r -> {
            r.setDevice(null);
            registrationRepository.save(r);
        });

        deviceRepository.delete(device);
        auditLogService.logAction(adminUsername, "DELETE_DEVICE", "BIOMETRIC_DEVICE", id.toString(),
                "Deleted device: " + device.getDeviceName());
    }

    private BiometricDeviceDTO mapDeviceToDTO(BiometricDevice device) {
        BiometricDeviceDTO dto = new BiometricDeviceDTO();
        dto.setId(device.getId());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceType(device.getDeviceType() != null ? device.getDeviceType() : "BIOMETRIC");
        dto.setDeviceModel(device.getDeviceModel());
        dto.setDeviceSerialNumber(device.getDeviceSerialNumber());
        dto.setIpAddress(device.getIpAddress());
        dto.setPort(device.getPort());
        dto.setLocation(device.getLocation());
        dto.setStatus(device.getStatus());
        dto.setDeviceUuid(device.getDeviceUuid() != null ? device.getDeviceUuid() : ("UUID-" + device.getDeviceSerialNumber()));
        dto.setManufacturer(device.getManufacturer() != null ? device.getManufacturer() : ("MOBILE".equalsIgnoreCase(device.getDeviceType()) ? "Samsung" : "ZKTeco"));
        dto.setDevicePlatform(device.getDevicePlatform() != null ? device.getDevicePlatform() : "Android");
        dto.setAndroidVersion(device.getAndroidVersion() != null ? device.getAndroidVersion() : "Android 14");
        dto.setAppVersion(device.getAppVersion() != null ? device.getAppVersion() : "v2.4.0-kiosk");
        dto.setDepartmentName(device.getDepartmentName() != null ? device.getDepartmentName() : "Engineering");
        dto.setConnectionType(device.getConnectionType() != null ? device.getConnectionType() : "Wi-Fi");
        dto.setCommunicationProtocol(device.getCommunicationProtocol() != null ? device.getCommunicationProtocol() : "TCP/IP");
        dto.setAssignedEmployeeId(device.getAssignedEmployeeId());
        dto.setAssignedEmployeeName(device.getAssignedEmployeeName());
        dto.setDeviceUsername(device.getDeviceUsername());
        dto.setWifiStatus(device.getWifiStatus() != null ? device.getWifiStatus() : "Connected (5GHz, -52dBm)");
        dto.setLastSyncTime(device.getLastSyncTime());
        dto.setCreatedAt(device.getCreatedAt());
        return dto;
    }
}
