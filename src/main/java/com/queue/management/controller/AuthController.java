package com.queue.management.controller;

import com.queue.management.dto.request.LoginRequest;
import com.queue.management.dto.request.RegisterRequest;
import com.queue.management.dto.request.ResetPasswordRequest;
import com.queue.management.dto.response.ApiResponse;
import com.queue.management.dto.response.LoginResponse;
import com.queue.management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // ─── REGISTER ──────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Passwords do not match!"));
        }

        String message = authService.registerStudent(
            request.getRollNumber(),
            request.getName(),
            request.getEmail(),
            request.getPassword()
        );

        log.info("Student registered: {}", request.getRollNumber());
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // ─── LOGIN ─────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        String token = authService.login(
            request.getIdentifier(),
            request.getPassword(),
            request.getUserType(),
            request.getSelectedCounter()
        );

        LoginResponse loginResponse = LoginResponse.builder()
            .token(token)
            .userType(request.getUserType())
            .identifier(request.getIdentifier())
            .message("Login successful!")
            .assignedCounter(request.getSelectedCounter())
            .build();

        log.info("User logged in: {}", request.getIdentifier());
        return ResponseEntity.ok(ApiResponse.success("Login successful!", loginResponse));
    }

    // ─── FORGOT PASSWORD ───────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @RequestParam String email) {

        // Always return OK to prevent email enumeration (service handles missing emails)
        try {
            authService.sendPasswordResetEmail(email);
        } catch (RuntimeException e) {
            log.warn("Forgot-password attempt for unknown email: {}", email);
        }

        return ResponseEntity.ok(ApiResponse.success(
            "If an account with that email exists, a reset link has been sent.", null
        ));
    }

    // ─── RESET PASSWORD ────────────────────────────────────────────────────

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Passwords do not match!"));
        }

        authService.resetPassword(request.getToken(), request.getNewPassword());

        log.info("Password successfully reset via token");
        return ResponseEntity.ok(ApiResponse.success(
            "Password reset successfully! You can now log in with your new password.", null
        ));
    }

    // ─── TEST ENDPOINT ─────────────────────────────────────────────────────

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(
            ApiResponse.success("API is working!", "Hello from Queue Management System!")
        );
    }
}