package com.company.employee_management.controller;

import com.company.employee_management.dto.ApiResponse;
import com.company.employee_management.dto.AuthResponse;
import com.company.employee_management.dto.LoginRequest;
import com.company.employee_management.security.UserPrincipal;
import com.company.employee_management.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@RequestBody Map<String, String> payload) {
        String target = payload.get("target");
        String type = payload.get("type"); // EMAIL or PHONE
        String code = authService.sendVerificationOtp(target, type);
        String msg = "EMAIL".equalsIgnoreCase(type)
                ? "6-digit OTP sent to Gmail (" + target + ")"
                : "6-digit OTP sent via SMS to " + target;
        return ResponseEntity.ok(ApiResponse.ok(msg, code));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Boolean>> verifyOtp(@RequestBody Map<String, String> payload) {
        String target = payload.get("target");
        String type = payload.get("type");
        String code = payload.get("code");
        boolean valid = authService.verifyOtp(target, type, code);
        if (valid) {
            String msg = "EMAIL".equalsIgnoreCase(type)
                    ? "Email verified successfully"
                    : "Phone number verified successfully — you may continue to fingerprint attendance or login";
            return ResponseEntity.ok(ApiResponse.ok(msg, true));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid or expired 6-digit OTP"));
        }
    }

    @PostMapping("/login-with-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithOtp(@RequestBody Map<String, String> payload) {
        String target = payload.get("target");
        String type = payload.get("type"); // PHONE or EMAIL
        String code = payload.get("code");
        AuthResponse response = authService.loginWithOtp(target, type, code);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified and logged in successfully", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserPrincipal>> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.ok("Authenticated user profile", currentUser));
    }
}
