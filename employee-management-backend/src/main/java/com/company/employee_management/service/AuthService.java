package com.company.employee_management.service;

import com.company.employee_management.dto.AuthResponse;
import com.company.employee_management.dto.LoginRequest;
import com.company.employee_management.entity.Employee;
import com.company.employee_management.entity.User;
import com.company.employee_management.exception.BadRequestException;
import com.company.employee_management.integration.email.EmailOtpService;
import com.company.employee_management.integration.sms.SmsOtpService;
import com.company.employee_management.repository.EmployeeRepository;
import com.company.employee_management.repository.UserRepository;
import com.company.employee_management.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailOtpService emailOtpService;
    private final SmsOtpService smsOtpService;
    private final AuditLogService auditLogService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserRepository userRepository,
                       EmployeeRepository employeeRepository,
                       EmailOtpService emailOtpService,
                       SmsOtpService smsOtpService,
                       AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.emailOtpService = emailOtpService;
        this.smsOtpService = smsOtpService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());
        String jwt = tokenProvider.generateToken(authentication, rememberMe);

        Optional<User> userOpt = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername());
        if (userOpt.isEmpty()) {
            userOpt = employeeRepository.findByEmployeeCode(request.getUsername().trim().toUpperCase())
                    .map(Employee::getUser);
        }
        User user = userOpt.orElseThrow(() -> new BadRequestException("User record not found"));


        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        Long empId = null;
        String empCode = null;
        String empName = user.getUsername();
        String photoUrl = null;

        Optional<Employee> empOpt = employeeRepository.findByUserId(user.getId());
        if (empOpt.isEmpty() && user.getEmail() != null) {
            empOpt = employeeRepository.findByEmail(user.getEmail());
        }

        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            empId = emp.getId();
            empCode = emp.getEmployeeCode();
            empName = emp.getFullName();
            photoUrl = emp.getProfilePhotoUrl();
        }

        auditLogService.logAction(user.getUsername(), "LOGIN", "USER", user.getId().toString(), "Successful user login");

        return new AuthResponse(
                jwt,
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName(),
                empId,
                empCode,
                empName,
                photoUrl
        );
    }

    public String sendVerificationOtp(String target, String type) {
        if ("EMAIL".equalsIgnoreCase(type)) {
            return emailOtpService.generateAndSendOtp(target);
        } else {
            return smsOtpService.generateAndSendOtp(target);
        }
    }

    public boolean verifyOtp(String target, String type, String code) {
        if ("EMAIL".equalsIgnoreCase(type)) {
            return emailOtpService.verifyOtp(target, code);
        } else {
            return smsOtpService.verifyOtp(target, code);
        }
    }

    @Transactional
    public AuthResponse loginWithOtp(String target, String type, String code) {
        boolean verified = verifyOtp(target, type, code);
        if (!verified) {
            throw new BadRequestException("Invalid or expired 6-digit OTP. Please request a new OTP.");
        }

        Optional<Employee> empOpt;
        if ("EMAIL".equalsIgnoreCase(type)) {
            empOpt = employeeRepository.findByEmail(target.trim().toLowerCase());
        } else {
            empOpt = employeeRepository.findByPhoneNumber(target.trim());
            if (empOpt.isEmpty()) {
                String cleanDigits = target.replaceAll("\\D+", "");
                empOpt = employeeRepository.findAll().stream()
                        .filter(e -> e.getPhoneNumber() != null && !cleanDigits.isEmpty()
                                && e.getPhoneNumber().replaceAll("\\D+", "").endsWith(cleanDigits.length() > 10
                                ? cleanDigits.substring(cleanDigits.length() - 10) : cleanDigits))
                        .findFirst();
            }
        }

        Employee emp = empOpt.orElseThrow(() ->
                new BadRequestException("OTP verified, but no employee account is registered with: " + target));

        User user = emp.getUser();
        if (user == null) {
            user = userRepository.findByEmail(emp.getEmail())
                    .orElseThrow(() -> new BadRequestException("User account not linked to employee: " + emp.getEmployeeCode()));
        }

        if (!Boolean.TRUE.equals(user.getIsActive()) || !Boolean.TRUE.equals(emp.getIsActive())) {
            throw new BadRequestException("Employee account is deactivated.");
        }

        com.company.employee_management.security.UserPrincipal principal =
                com.company.employee_management.security.UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = tokenProvider.generateToken(auth, true);

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        auditLogService.logAction(user.getUsername(), "OTP_LOGIN", "USER", user.getId().toString(),
                "Successful employee login via " + type.toUpperCase() + " OTP");

        return new AuthResponse(
                jwt,
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName(),
                emp.getId(),
                emp.getEmployeeCode(),
                emp.getFullName(),
                emp.getProfilePhotoUrl()
        );
    }
}
