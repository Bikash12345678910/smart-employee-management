package com.company.employee_management.service;

import com.company.employee_management.dto.EmployeeCreateRequest;
import com.company.employee_management.dto.EmployeeDTO;
import com.company.employee_management.dto.EmployeeUpdateRequest;
import com.company.employee_management.entity.*;
import com.company.employee_management.exception.BadRequestException;
import com.company.employee_management.exception.ResourceNotFoundException;
import com.company.employee_management.integration.biometric.BiometricDeviceGateway;
import com.company.employee_management.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BiometricRegistrationRepository biometricRegistrationRepository;
    private final BiometricDeviceRepository biometricDeviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final BiometricDeviceGateway biometricDeviceGateway;
    private final AuditLogService auditLogService;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceEventRepository eventRepository;
    private final EmployeeBreakRepository breakRepository;
    private final PayslipRepository payslipRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           DesignationRepository designationRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           BiometricRegistrationRepository biometricRegistrationRepository,
                           BiometricDeviceRepository biometricDeviceRepository,
                           PasswordEncoder passwordEncoder,
                           BiometricDeviceGateway biometricDeviceGateway,
                           AuditLogService auditLogService,
                           AttendanceRepository attendanceRepository,
                           AttendanceEventRepository eventRepository,
                           EmployeeBreakRepository breakRepository,
                           PayslipRepository payslipRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.biometricRegistrationRepository = biometricRegistrationRepository;
        this.biometricDeviceRepository = biometricDeviceRepository;
        this.passwordEncoder = passwordEncoder;
        this.biometricDeviceGateway = biometricDeviceGateway;
        this.auditLogService = auditLogService;
        this.attendanceRepository = attendanceRepository;
        this.eventRepository = eventRepository;
        this.breakRepository = breakRepository;
        this.payslipRepository = payslipRepository;
    }

    public Page<EmployeeDTO> getEmployees(String search, Long departmentId, String status, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) ? status.trim() : null;

        Page<Employee> page = employeeRepository.findWithFilters(cleanSearch, departmentId, cleanStatus, pageable);
        return page.map(this::mapToDTO);
    }

    public List<EmployeeDTO> getAllEmployeesList() {
        return employeeRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToDTO(employee);
    }

    public EmployeeDTO getEmployeeByCode(String code) {
        Employee employee = employeeRepository.findByEmployeeCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + code));
        return mapToDTO(employee);
    }

    public EmployeeDTO getEmployeeByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for user id: " + userId));
        return mapToDTO(employee);
    }

    @Transactional
    public EmployeeDTO createEmployee(EmployeeCreateRequest req, String adminUsername) {
        // Validation
        if (employeeRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("An employee with email " + req.getEmail() + " already exists");
        }
        if (employeeRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new BadRequestException("An employee with phone number " + req.getPhoneNumber() + " already exists");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username " + req.getUsername() + " is already taken");
        }

        // Auto-generate employee code if missing
        String empCode = req.getEmployeeCode();
        if (empCode == null || empCode.trim().isEmpty()) {
            Integer maxSuffix = employeeRepository.findMaxEmployeeCodeSuffix();
            int nextNumber = (maxSuffix != null ? maxSuffix : 0) + 1;
            empCode = String.format("EMP%03d", nextNumber);
        }

        Long deptId = req.getDepartmentId();
        Department department = (deptId != null)
                ? departmentRepository.findById(deptId).orElse(departmentRepository.findAll().stream().findFirst().orElse(null))
                : departmentRepository.findAll().stream().findFirst().orElse(null);

        Long desigId = req.getDesignationId();
        Designation designation = (desigId != null)
                ? designationRepository.findById(desigId).orElse(designationRepository.findAll().stream().findFirst().orElse(null))
                : designationRepository.findAll().stream().findFirst().orElse(null);

        // Create user login account
        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_EMPLOYEE", "Employee Portal Role")));

        User user = new User(
                req.getUsername(),
                req.getEmail(),
                passwordEncoder.encode(req.getPassword()),
                employeeRole
        );
        user = userRepository.save(user);

        // Build Employee
        Employee emp = new Employee();
        emp.setEmployeeCode(empCode);
        emp.setUser(user);
        emp.setFirstName(req.getFirstName());
        emp.setLastName(req.getLastName());
        emp.setEmail(req.getEmail());
        emp.setPhoneNumber(req.getPhoneNumber());
        emp.setDateOfBirth(req.getDateOfBirth());
        emp.setGender(req.getGender());
        emp.setAddress(req.getAddress());
        emp.setProfilePhotoUrl(req.getProfilePhotoUrl() != null && !req.getProfilePhotoUrl().isEmpty() ?
                req.getProfilePhotoUrl() : "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80");

        emp.setDepartment(department);
        emp.setDesignation(designation);
        emp.setJoiningDate(req.getJoiningDate());
        emp.setEmploymentStatus(req.getEmploymentStatus() != null ? req.getEmploymentStatus() : "ACTIVE");
        emp.setAgreedMonthlySalary(req.getAgreedMonthlySalary());

        emp.setAadhaarNumber(req.getAadhaarNumber());
        emp.setPanNumber(req.getPanNumber());
        emp.setBankAccountNumber(req.getBankAccountNumber());
        emp.setBankIfscCode(req.getBankIfscCode());
        emp.setBankName(req.getBankName() != null ? req.getBankName() : "HDFC Bank");

        emp.setIsPhoneVerified(req.getPhoneOtp() != null && !req.getPhoneOtp().isEmpty());
        emp.setIsEmailVerified(req.getEmailOtp() != null && !req.getEmailOtp().isEmpty());
        emp.setIsActive(true);

        emp = employeeRepository.save(emp);

        // Biometric enrollments if provided
        if (Boolean.TRUE.equals(req.getRegisterFace()) && req.getLiveFaceData() != null) {
            BiometricRegistration faceReg = new BiometricRegistration();
            faceReg.setEmployee(emp);
            faceReg.setBiometricType("FACE");
            faceReg.setFaceSampleData(req.getLiveFaceData());
            faceReg.setTemplateHash(biometricDeviceGateway.hashBiometricTemplate(req.getLiveFaceData(), emp.getEmployeeCode()));
            faceReg.setIsActive(true);
            biometricRegistrationRepository.save(faceReg);
        }

        if (Boolean.TRUE.equals(req.getRegisterFingerprint())) {
            BiometricRegistration fpReg = new BiometricRegistration();
            fpReg.setEmployee(emp);
            fpReg.setBiometricType("FINGERPRINT");
            if (req.getFingerprintDeviceId() != null) {
                biometricDeviceRepository.findById(req.getFingerprintDeviceId()).ifPresent(fpReg::setDevice);
            }
            fpReg.setTemplateHash(biometricDeviceGateway.hashBiometricTemplate("FINGERPRINT_SAMPLE_" + emp.getEmployeeCode(), emp.getEmployeeCode()));
            fpReg.setFingerprintSlot(emp.getId().intValue());
            fpReg.setIsActive(true);
            biometricRegistrationRepository.save(fpReg);
        }

        auditLogService.logAction(adminUsername, "CREATE_EMPLOYEE", "EMPLOYEE", emp.getId().toString(),
                "Registered employee " + emp.getFullName() + " (" + emp.getEmployeeCode() + ")");

        return mapToDTO(emp);
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateRequest req, String adminUsername) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (req.getFirstName() != null) emp.setFirstName(req.getFirstName());
        if (req.getLastName() != null) emp.setLastName(req.getLastName());
        if (req.getPhoneNumber() != null) emp.setPhoneNumber(req.getPhoneNumber());
        if (req.getAddress() != null) emp.setAddress(req.getAddress());
        if (req.getProfilePhotoUrl() != null) emp.setProfilePhotoUrl(req.getProfilePhotoUrl());

        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new BadRequestException("Department not found with id: " + req.getDepartmentId()));
            emp.setDepartment(dept);
        }

        if (req.getDesignationId() != null) {
            Designation desig = designationRepository.findById(req.getDesignationId())
                    .orElseThrow(() -> new BadRequestException("Designation not found with id: " + req.getDesignationId()));
            emp.setDesignation(desig);
        }

        if (req.getEmploymentStatus() != null) emp.setEmploymentStatus(req.getEmploymentStatus());
        if (req.getAgreedMonthlySalary() != null) emp.setAgreedMonthlySalary(req.getAgreedMonthlySalary());
        if (req.getBankAccountNumber() != null) emp.setBankAccountNumber(req.getBankAccountNumber());
        if (req.getBankIfscCode() != null) emp.setBankIfscCode(req.getBankIfscCode());
        if (req.getBankName() != null) emp.setBankName(req.getBankName());
        if (req.getIsActive() != null) {
            emp.setIsActive(req.getIsActive());
            if (emp.getUser() != null) {
                emp.getUser().setIsActive(req.getIsActive());
                userRepository.save(emp.getUser());
            }
        }

        emp = employeeRepository.save(emp);
        auditLogService.logAction(adminUsername, "UPDATE_EMPLOYEE", "EMPLOYEE", emp.getId().toString(),
                "Updated employee details for " + emp.getEmployeeCode());

        return mapToDTO(emp);
    }

    @Transactional
    public void deleteEmployee(Long id, String adminUsername) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        String code = emp.getEmployeeCode();
        Long userId = emp.getUser() != null ? emp.getUser().getId() : null;

        // Clean up all child dependencies to prevent foreign key constraint violations
        breakRepository.deleteByEmployeeId(id);
        eventRepository.deleteByEmployeeId(id);
        attendanceRepository.deleteByEmployeeId(id);
        biometricRegistrationRepository.deleteByEmployeeId(id);
        payslipRepository.deleteByEmployeeId(id);

        employeeRepository.delete(emp);
        if (userId != null) {
            userRepository.deleteById(userId);
        }

        auditLogService.logAction(adminUsername, "DELETE_EMPLOYEE", "EMPLOYEE", id.toString(),
                "Deleted employee record: " + code);
    }

    @Transactional
    public EmployeeDTO toggleActiveStatus(Long id, String adminUsername) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        boolean newStatus = !Boolean.TRUE.equals(emp.getIsActive());
        emp.setIsActive(newStatus);
        emp.setEmploymentStatus(newStatus ? "ACTIVE" : "SUSPENDED");

        if (emp.getUser() != null) {
            emp.getUser().setIsActive(newStatus);
            userRepository.save(emp.getUser());
        }

        emp = employeeRepository.save(emp);
        auditLogService.logAction(adminUsername, "TOGGLE_STATUS", "EMPLOYEE", id.toString(),
                "Changed employee status to " + (newStatus ? "ACTIVE" : "INACTIVE"));

        return mapToDTO(emp);
    }

    public String exportEmployeesToCsv() {
        List<Employee> list = employeeRepository.findAll();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Employee ID,Full Name,Email,Phone,Department,Designation,Joining Date,Status,Agreed Salary,Aadhaar (Masked),PAN (Masked),Bank Account (Masked)");
        for (Employee e : list) {
            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%.2f\",\"%s\",\"%s\",\"%s\"%n",
                    e.getEmployeeCode(),
                    e.getFullName(),
                    e.getEmail(),
                    e.getPhoneNumber(),
                    e.getDepartment() != null ? e.getDepartment().getName() : "",
                    e.getDesignation() != null ? e.getDesignation().getTitle() : "",
                    e.getJoiningDate(),
                    e.getEmploymentStatus(),
                    e.getAgreedMonthlySalary(),
                    EmployeeDTO.maskAadhaar(e.getAadhaarNumber()),
                    EmployeeDTO.maskPan(e.getPanNumber()),
                    EmployeeDTO.maskBankAccount(e.getBankAccountNumber())
            );
        }
        return sw.toString();
    }

    public EmployeeDTO mapToDTO(Employee emp) {
        if (emp == null) return null;
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(emp.getId());
        dto.setEmployeeCode(emp.getEmployeeCode());
        if (emp.getUser() != null) {
            dto.setUserId(emp.getUser().getId());
            dto.setUsername(emp.getUser().getUsername());
        }
        dto.setFirstName(emp.getFirstName());
        dto.setLastName(emp.getLastName());
        dto.setFullName(emp.getFullName());
        dto.setEmail(emp.getEmail());
        dto.setPhoneNumber(emp.getPhoneNumber());
        dto.setDateOfBirth(emp.getDateOfBirth());
        dto.setGender(emp.getGender());
        dto.setAddress(emp.getAddress());
        dto.setProfilePhotoUrl(emp.getProfilePhotoUrl());

        if (emp.getDepartment() != null) {
            dto.setDepartmentId(emp.getDepartment().getId());
            dto.setDepartmentName(emp.getDepartment().getName());
            dto.setDepartmentCode(emp.getDepartment().getDepartmentCode());
        }
        if (emp.getDesignation() != null) {
            dto.setDesignationId(emp.getDesignation().getId());
            dto.setDesignationTitle(emp.getDesignation().getTitle());
        }

        dto.setJoiningDate(emp.getJoiningDate());
        dto.setEmploymentStatus(emp.getEmploymentStatus());
        dto.setAgreedMonthlySalary(emp.getAgreedMonthlySalary());

        // Masked Financials
        dto.setAadhaarMasked(EmployeeDTO.maskAadhaar(emp.getAadhaarNumber()));
        dto.setPanMasked(EmployeeDTO.maskPan(emp.getPanNumber()));
        dto.setBankAccountMasked(EmployeeDTO.maskBankAccount(emp.getBankAccountNumber()));
        dto.setBankIfscCode(emp.getBankIfscCode());
        dto.setBankName(emp.getBankName());

        // Raw numbers (for authorized admin review)
        dto.setAadhaarNumber(emp.getAadhaarNumber());
        dto.setPanNumber(emp.getPanNumber());
        dto.setBankAccountNumber(emp.getBankAccountNumber());

        dto.setIsPhoneVerified(emp.getIsPhoneVerified());
        dto.setIsEmailVerified(emp.getIsEmailVerified());
        dto.setIsActive(emp.getIsActive());
        dto.setCreatedAt(emp.getCreatedAt());
        dto.setUpdatedAt(emp.getUpdatedAt());

        dto.setIsFaceRegistered(biometricRegistrationRepository.existsByEmployeeIdAndBiometricType(emp.getId(), "FACE"));
        dto.setIsFingerprintRegistered(biometricRegistrationRepository.existsByEmployeeIdAndBiometricType(emp.getId(), "FINGERPRINT"));

        return dto;
    }
}
