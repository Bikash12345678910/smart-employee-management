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

    public java.util.Map<String, Object> sendVerificationOtpWithConfig(java.util.Map<String, String> payload) {
        String target = payload.get("target");
        String type = payload.get("type");
        if ("EMAIL".equalsIgnoreCase(type)) {
            return emailOtpService.generateAndSendOtpWithConfig(
                    target,
                    payload.get("gmailSenderEmail"),
                    payload.get("gmailAppPassword")
            );
        } else {
            return smsOtpService.generateAndSendOtpWithConfig(
                    target,
                    payload.get("smsProvider"),
                    payload.get("fast2SmsApiKey"),
                    payload.get("twilioAccountSid"),
                    payload.get("twilioAuthToken"),
                    payload.get("twilioFromNumber")
            );
        }
    }

    public boolean verifyOtp(String target, String type, String code) {
        if ("EMAIL".equalsIgnoreCase(type)) {
            return emailOtpService.verifyOtp(target, code);
        } else {
            return smsOtpService.verifyOtp(target, code);
        }
    }
}
