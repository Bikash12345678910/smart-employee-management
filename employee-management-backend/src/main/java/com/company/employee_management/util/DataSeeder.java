package com.company.employee_management.util;

import com.company.employee_management.entity.*;
import com.company.employee_management.integration.biometric.BiometricDeviceGateway;
import com.company.employee_management.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final EmployeeRepository employeeRepository;
    private final BiometricDeviceRepository deviceRepository;
    private final BiometricRegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeBreakRepository breakRepository;
    private final HolidayRepository holidayRepository;
    private final PayslipRepository payslipRepository;
    private final CompanySettingRepository companySettingRepository;
    private final PasswordEncoder passwordEncoder;
    private final BiometricDeviceGateway gateway;

    public DataSeeder(RoleRepository roleRepository,
                      UserRepository userRepository,
                      DepartmentRepository departmentRepository,
                      DesignationRepository designationRepository,
                      EmployeeRepository employeeRepository,
                      BiometricDeviceRepository deviceRepository,
                      BiometricRegistrationRepository registrationRepository,
                      AttendanceRepository attendanceRepository,
                      EmployeeBreakRepository breakRepository,
                      HolidayRepository holidayRepository,
                      PayslipRepository payslipRepository,
                      CompanySettingRepository companySettingRepository,
                      PasswordEncoder passwordEncoder,
                      BiometricDeviceGateway gateway) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.employeeRepository = employeeRepository;
        this.deviceRepository = deviceRepository;
        this.registrationRepository = registrationRepository;
        this.attendanceRepository = attendanceRepository;
        this.breakRepository = breakRepository;
        this.holidayRepository = holidayRepository;
        this.payslipRepository = payslipRepository;
        this.companySettingRepository = companySettingRepository;
        this.passwordEncoder = passwordEncoder;
        this.gateway = gateway;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing Seed Data for Smart Employee Management System...");

        // 1. Roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN", "Administrator with full system privileges")));
        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_EMPLOYEE", "Standard employee portal access")));

        // 2. Company Settings
        if (companySettingRepository.count() == 0) {
            CompanySetting s = new CompanySetting();
            s.setCompanyName("Apex Global Technologies Inc.");
            s.setCompanyEmail("admin@apextech.com");
            s.setContactNumber("+1 (800) 555-0199");
            s.setCompanyAddress("100 Tech Park Boulevard, Suite 500, Silicon City, CA 94025");
            s.setShiftStartTime(LocalTime.of(9, 0));
            s.setShiftEndTime(LocalTime.of(18, 0));
            s.setLateArrivalGraceMinutes(15);
            s.setHalfDayThresholdHours(4);
            s.setFullDayWorkingHours(8);
            s.setWeeklyOffDays("SUNDAY");
            s.setEnableFaceAttendance(true);
            s.setEnableFingerprintAttendance(true);
            s.setEnablePasswordAttendance(true);
            s.setAllowManualAttendance(true);
            s.setStandardWorkingDays(26);
            companySettingRepository.save(s);
        }

        // 3. Admin Users (2 Admin Accounts Configured)
        User admin1 = userRepository.findByUsername("admin").orElse(null);
        if (admin1 == null) {
            admin1 = new User("admin", "admin@apextech.com", passwordEncoder.encode("Admin@123"), adminRole);
            userRepository.save(admin1);
            log.info("Created Default Admin 1 -> username: admin, email: admin@apextech.com, password: Admin@123");
        } else {
            admin1.setPassword(passwordEncoder.encode("Admin@123"));
            admin1.setRole(adminRole);
            userRepository.save(admin1);
            log.info("Refreshed Default Admin 1 -> username: admin, password: Admin@123");
        }

        User admin2 = userRepository.findByUsername("admin2").orElse(null);
        if (admin2 == null) {
            admin2 = new User("admin2", "admin2@apextech.com", passwordEncoder.encode("Admin@123"), adminRole);
            userRepository.save(admin2);
            log.info("Created Default Admin 2 -> username: admin2, email: admin2@apextech.com, password: Admin@123");
        } else {
            admin2.setPassword(passwordEncoder.encode("Admin@123"));
            admin2.setRole(adminRole);
            userRepository.save(admin2);
            log.info("Refreshed Default Admin 2 -> username: admin2, password: Admin@123");
        }

        // Ensure all seed employee users have valid password hash
        userRepository.findAll().forEach(u -> {
            if (!"admin".equals(u.getUsername()) && !"admin2".equals(u.getUsername())) {
                u.setPassword(passwordEncoder.encode("Employee@123"));
                userRepository.save(u);
            }
        });

        // 4. Departments
        Department deptEng = departmentRepository.findByDepartmentCode("ENG")
                .orElseGet(() -> departmentRepository.save(new Department("ENG", "Engineering & Technology", "Software architecture, cloud platforms, DevOps")));
        Department deptHr = departmentRepository.findByDepartmentCode("HR")
                .orElseGet(() -> departmentRepository.save(new Department("HR", "Human Resources", "People operations, talent, culture")));
        Department deptFin = departmentRepository.findByDepartmentCode("FIN")
                .orElseGet(() -> departmentRepository.save(new Department("FIN", "Finance & Accounting", "Payroll, audit, taxation")));
        Department deptDes = departmentRepository.findByDepartmentCode("DES")
                .orElseGet(() -> departmentRepository.save(new Department("DES", "Product & UI/UX Design", "Product design, UX research")));

        // 5. Designations
        Designation desigPrincipal = designationRepository.save(new Designation("Principal Software Engineer", deptEng, "Core architecture"));
        Designation desigFrontend = designationRepository.save(new Designation("Senior Frontend Engineer", deptEng, "Web platforms & React"));
        Designation desigBackend = designationRepository.save(new Designation("Senior Backend Engineer", deptEng, "Java Spring & Microservices"));
        Designation desigHrDir = designationRepository.save(new Designation("HR Director", deptHr, "People strategy"));
        Designation desigFinCtrl = designationRepository.save(new Designation("Financial Controller", deptFin, "Financial audit"));
        Designation desigLeadDes = designationRepository.save(new Designation("Lead Product Designer", deptDes, "Design systems"));

        // 6. Biometric Devices
        BiometricDevice dev1 = null;
        BiometricDevice dev2 = null;
        if (deviceRepository.count() == 0) {
            dev1 = new BiometricDevice();
            dev1.setDeviceName("ZKTeco BioStation Pro (Demo)");
            dev1.setDeviceType("BIOMETRIC");
            dev1.setDeviceModel("ZKTeco uFace800");
            dev1.setDeviceSerialNumber("ZK-UF800-98421");
            dev1.setIpAddress("192.168.1.120");
            dev1.setPort(4370);
            dev1.setCommunicationProtocol("TCP/IP");
            dev1.setConnectionType("LAN");
            dev1.setManufacturer("ZKTeco");
            dev1.setLocation("Main Entrance Gate");
            dev1.setStatus("ONLINE");
            dev1.setLastSyncTime(LocalDateTime.now());
            dev1 = deviceRepository.save(dev1);

            dev2 = new BiometricDevice();
            dev2.setDeviceName("Samsung Galaxy Kiosk Tab (Demo)");
            dev2.setDeviceType("MOBILE");
            dev2.setDeviceModel("Galaxy Tab A9+");
            dev2.setDeviceSerialNumber("MOB-TAB-A9-2026");
            dev2.setDeviceUuid("MOB-TAB-A9-2026");
            dev2.setDevicePlatform("Android");
            dev2.setIpAddress("192.168.1.50");
            dev2.setPort(8080);
            dev2.setCommunicationProtocol("HTTPS/REST");
            dev2.setConnectionType("Wi-Fi");
            dev2.setManufacturer("Samsung");
            dev2.setAndroidVersion("Android 14");
            dev2.setAppVersion("v2.4.0-kiosk");
            dev2.setLocation("Reception Desk");
            dev2.setStatus("ONLINE");
            dev2.setLastSyncTime(LocalDateTime.now());
            dev2 = deviceRepository.save(dev2);
        } else {
            List<BiometricDevice> devices = deviceRepository.findAll();
            dev1 = devices.get(0);
            if (devices.size() > 1) dev2 = devices.get(1);
        }

        // 7. Holidays (for 2026)
        if (holidayRepository.count() == 0) {
            holidayRepository.save(new Holiday("New Year Day", LocalDate.of(2026, 1, 1), "Thursday", "Global celebration", 2026));
            holidayRepository.save(new Holiday("Republic Day", LocalDate.of(2026, 1, 26), "Monday", "National holiday", 2026));
            holidayRepository.save(new Holiday("Holi (Festival of Colours)", LocalDate.of(2026, 3, 4), "Wednesday", "Spring festival", 2026));
            holidayRepository.save(new Holiday("Good Friday", LocalDate.of(2026, 4, 3), "Friday", "Easter holiday", 2026));
            holidayRepository.save(new Holiday("Labour Day", LocalDate.of(2026, 5, 1), "Friday", "Workers day", 2026));
            holidayRepository.save(new Holiday("Independence Day", LocalDate.of(2026, 8, 15), "Saturday", "National independence day", 2026));
            holidayRepository.save(new Holiday("Gandhi Jayanti", LocalDate.of(2026, 10, 2), "Friday", "Mahatma Gandhi anniversary", 2026));
            holidayRepository.save(new Holiday("Diwali (Festival of Lights)", LocalDate.of(2026, 11, 8), "Sunday", "Deepavali celebration", 2026));
            holidayRepository.save(new Holiday("Christmas Day", LocalDate.of(2026, 12, 25), "Friday", "Christmas holiday", 2026));
        }

        // 8. Sample Employees
        if (employeeRepository.count() == 0) {
            createSampleEmployee("EMP001", "john.doe", "john.doe@apextech.com", "John", "Doe",
                    "+1 (555) 234-5678", LocalDate.of(1990, 5, 14), "MALE", "452 Silicon Ave, Apt 4B, San Jose, CA",
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
                    deptEng, desigPrincipal, LocalDate.of(2022, 3, 1), new BigDecimal("8500.00"),
                    "987456321012", "ABCDE1234F", "98765432109876", "HDFC0001234", "HDFC Bank", employeeRole, dev1);

            createSampleEmployee("EMP002", "sarah.j", "sarah.j@apextech.com", "Sarah", "Jenkins",
                    "+1 (555) 345-6789", LocalDate.of(1993, 8, 22), "FEMALE", "120 Market St, San Francisco, CA",
                    "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=80",
                    deptEng, desigFrontend, LocalDate.of(2022, 6, 15), new BigDecimal("7200.00"),
                    "876543210987", "BCDEF2345G", "87654321098765", "ICIC0002345", "ICICI Bank", employeeRole, dev1);

            createSampleEmployee("EMP003", "david.k", "david.k@apextech.com", "David", "Kumar",
                    "+1 (555) 456-7890", LocalDate.of(1988, 11, 30), "MALE", "78 Oakwood Dr, Palo Alto, CA",
                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
                    deptHr, desigHrDir, LocalDate.of(2021, 1, 10), new BigDecimal("6800.00"),
                    "765432109876", "CDEFG3456H", "76543210987654", "SBIN0003456", "State Bank", employeeRole, dev2);

            createSampleEmployee("EMP004", "elena.r", "elena.r@apextech.com", "Elena", "Rostova",
                    "+1 (555) 567-8901", LocalDate.of(1991, 2, 18), "FEMALE", "330 Fremont St, San Francisco, CA",
                    "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80",
                    deptFin, desigFinCtrl, LocalDate.of(2022, 9, 1), new BigDecimal("7500.00"),
                    "654321098765", "DEFGH4567J", "65432109876543", "CITI0004567", "Citibank", employeeRole, dev1);

            createSampleEmployee("EMP005", "marcus.v", "marcus.v@apextech.com", "Marcus", "Vance",
                    "+1 (555) 678-9012", LocalDate.of(1994, 9, 5), "MALE", "210 Mission St, San Francisco, CA",
                    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80",
                    deptDes, desigLeadDes, LocalDate.of(2023, 2, 1), new BigDecimal("6500.00"),
                    "543210987654", "EFGHI5678K", "54321098765432", "BARC0005678", "Barclays", employeeRole, dev2);

            createSampleEmployee("EMP006", "priya.s", "priya.s@apextech.com", "Priya", "Sharma",
                    "+1 (555) 789-0123", LocalDate.of(1992, 12, 10), "FEMALE", "89 Castro St, Mountain View, CA",
                    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80",
                    deptEng, desigBackend, LocalDate.of(2023, 4, 15), new BigDecimal("7400.00"),
                    "432109876543", "FGHIJ6789L", "43210987654321", "WELL0006789", "Wells Fargo", employeeRole, dev1);

            createSampleEmployee("EMP007", "biswanath.m", "biswanath.m@apextech.com", "Biswanath", "Manna",
                    "+1 (555) 999-8877", LocalDate.of(1989, 7, 20), "MALE", "770 Innovation Way, Silicon City, CA",
                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
                    deptEng, desigPrincipal, LocalDate.of(2022, 1, 15), new BigDecimal("15000.00"),
                    "998877665544", "MNBVC9876X", "99887766554433", "HDFC0001234", "HDFC Bank", employeeRole, dev1);

            // 9. Create past attendance records for current month so dashboard charts and matrix are lively
            seedAttendanceRecords();
        } else if (!employeeRepository.existsByEmail("biswanath.m@apextech.com")) {
            createSampleEmployee("EMP007", "biswanath.m", "biswanath.m@apextech.com", "Biswanath", "Manna",
                    "+1 (555) 999-8877", LocalDate.of(1989, 7, 20), "MALE", "770 Innovation Way, Silicon City, CA",
                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
                    deptEng, desigPrincipal, LocalDate.of(2022, 1, 15), new BigDecimal("15000.00"),
                    "998877665544", "MNBVC9876X", "99887766554433", "HDFC0001234", "HDFC Bank", employeeRole, dev1);
        }

        log.info("Seed Data Initialization Completed Successfully.");
    }

    private void createSampleEmployee(String code, String username, String email, String fName, String lName,
                                      String phone, LocalDate dob, String gender, String address, String photo,
                                      Department dept, Designation desig, LocalDate joining, BigDecimal salary,
                                      String aadhaar, String pan, String bankAcc, String ifsc, String bankName,
                                      Role role, BiometricDevice device) {

        User user = new User(username, email, passwordEncoder.encode("Employee@123"), role);
        user = userRepository.save(user);

        Employee emp = new Employee();
        emp.setEmployeeCode(code);
        emp.setUser(user);
        emp.setFirstName(fName);
        emp.setLastName(lName);
        emp.setEmail(email);
        emp.setPhoneNumber(phone);
        emp.setDateOfBirth(dob);
        emp.setGender(gender);
        emp.setAddress(address);
        emp.setProfilePhotoUrl(photo);
        emp.setDepartment(dept);
        emp.setDesignation(desig);
        emp.setJoiningDate(joining);
        emp.setEmploymentStatus("ACTIVE");
        emp.setAgreedMonthlySalary(salary);
        emp.setAadhaarNumber(aadhaar);
        emp.setPanNumber(pan);
        emp.setBankAccountNumber(bankAcc);
        emp.setBankIfscCode(ifsc);
        emp.setBankName(bankName);
        emp.setIsPhoneVerified(true);
        emp.setIsEmailVerified(true);
        emp.setIsActive(true);

        emp = employeeRepository.save(emp);

        // Biometric enrollments
        BiometricRegistration faceReg = new BiometricRegistration();
        faceReg.setEmployee(emp);
        faceReg.setBiometricType("FACE");
        faceReg.setDevice(device);
        faceReg.setTemplateHash(gateway.hashBiometricTemplate("FACE_EMBEDDING_" + code, code));
        faceReg.setIsActive(true);
        registrationRepository.save(faceReg);

        BiometricRegistration fpReg = new BiometricRegistration();
        fpReg.setEmployee(emp);
        fpReg.setBiometricType("FINGERPRINT");
        fpReg.setDevice(device);
        fpReg.setTemplateHash(gateway.hashBiometricTemplate("FP_TEMPLATE_" + code, code));
        fpReg.setFingerprintSlot(emp.getId().intValue());
        fpReg.setIsActive(true);
        registrationRepository.save(fpReg);
    }

    private void seedAttendanceRecords() {
        LocalDate today = LocalDate.now();
        List<Employee> emps = employeeRepository.findAll();

        // Seed 14 days of records
        for (int d = 14; d >= 0; d--) {
            LocalDate date = today.minusDays(d);
            boolean isSunday = date.getDayOfWeek().name().equals("SUNDAY");

            for (int i = 0; i < emps.size(); i++) {
                Employee emp = emps.get(i);
                Attendance a = new Attendance();
                a.setEmployee(emp);
                a.setAttendanceDate(date);

                if (isSunday) {
                    a.setAttendanceStatus("HOLIDAY");
                    a.setAttendanceVia("SYSTEM");
                } else if (d == 0) { // Today
                    if (i < 4) {
                        a.setCheckInTime(LocalTime.of(8, 50).plusMinutes(i * 5));
                        a.setAttendanceVia(i % 2 == 0 ? "FACE_RECOGNITION" : "FINGERPRINT");
                        if (i == 3) {
                            a.setAttendanceStatus("LATE");
                            a.setLateMinutes(20);
                        } else {
                            a.setAttendanceStatus("PRESENT");
                            a.setLateMinutes(0);
                        }
                    } else if (i == 4) {
                        a.setAttendanceStatus("ON_LEAVE");
                        a.setAttendanceVia("SYSTEM");
                    } else {
                        a.setAttendanceStatus("ABSENT");
                        a.setAttendanceVia("SYSTEM");
                    }
                } else { // Past days
                    if ((i + d) % 9 == 0) {
                        a.setAttendanceStatus("ON_LEAVE");
                        a.setAttendanceVia("SYSTEM");
                    } else if ((i + d) % 11 == 0) {
                        a.setAttendanceStatus("ABSENT");
                        a.setAttendanceVia("SYSTEM");
                    } else {
                        boolean isLate = (i + d) % 5 == 0;
                        a.setCheckInTime(isLate ? LocalTime.of(9, 25) : LocalTime.of(8, 55));
                        a.setCheckOutTime(LocalTime.of(18, 5));
                        a.setWorkingHours(8);
                        a.setWorkingMinutes(40);
                        a.setAttendanceStatus(isLate ? "LATE" : "PRESENT");
                        a.setLateMinutes(isLate ? 25 : 0);
                        a.setAttendanceVia(i % 2 == 0 ? "FACE_RECOGNITION" : "FINGERPRINT");
                        a.setBreakCount(1);
                        a.setTotalBreakMinutes(35);
                    }
                }
                attendanceRepository.save(a);
            }
        }

        // Seed sample finalized payslip for previous month
        int prevMonth = today.minusMonths(1).getMonthValue();
        int prevYear = today.minusMonths(1).getYear();
        for (Employee emp : emps) {
            Payslip p = new Payslip();
            p.setEmployee(emp);
            p.setSalaryMonth(prevMonth);
            p.setSalaryYear(prevYear);
            p.setPayslipNumber(String.format("PAY-%d-%02d-%s", prevYear, prevMonth, emp.getEmployeeCode()));
            p.setAgreedMonthlySalary(emp.getAgreedMonthlySalary());
            p.setTotalWorkingDays(26);
            p.setPresentDays(24);
            p.setAbsentDays(2);
            p.setLeaveDays(0);
            p.setLateMinutes(35);
            p.setTotalWorkingHours(new BigDecimal("192.50"));
            p.setGrossEarnings(emp.getAgreedMonthlySalary());
            p.setTotalDeductions(new BigDecimal("1150.00"));
            p.setNetPayableSalary(emp.getAgreedMonthlySalary().subtract(new BigDecimal("1150.00")));
            p.setStatus("FINALIZED");
            p.setPaymentDate(today.minusDays(5));
            payslipRepository.save(p);
        }
    }
}
