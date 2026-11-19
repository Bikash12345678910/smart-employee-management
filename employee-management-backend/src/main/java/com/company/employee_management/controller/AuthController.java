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
        return ResponseEntity.ok(ApiResponse.ok("OTP dispatched successfully", code));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Boolean>> verifyOtp(@RequestBody Map<String, String> payload) {
        String target = payload.get("target");
        String type = payload.get("type");
        String code = payload.get("code");
        boolean valid = authService.verifyOtp(target, type, code);
        if (valid) {
            return ResponseEntity.ok(ApiResponse.ok("OTP verified successfully", true));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid or expired OTP"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserPrincipal>> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.ok("Authenticated user profile", currentUser));
    }
}
