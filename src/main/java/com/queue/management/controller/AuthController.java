package com.queue.management.controller;

import com.queue.management.dto.request.LoginRequest;
import com.queue.management.dto.request.RegisterRequest;
import com.queue.management.dto.response.ApiResponse;
import com.queue.management.dto.response.LoginResponse;
import com.queue.management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

        // Check passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Passwords do not match!"));
        }

        // Register student
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

        // Login and get JWT token
        String token = authService.login(
            request.getIdentifier(),
            request.getPassword(),
            request.getUserType(),
            request.getSelectedCounter()
        );

        // Build response
        LoginResponse loginResponse = LoginResponse.builder()
            .token(token)
            .userType(request.getUserType())
            .identifier(request.getIdentifier())
            .message("Login successful!")
            .assignedCounter(request.getSelectedCounter())
            .build();

        log.info("User logged in: {}", request.getIdentifier());
        return ResponseEntity.ok(
            ApiResponse.success("Login successful!", loginResponse)
        );
    }

    // ─── FORGOT PASSWORD ───────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @RequestParam String email) {

        // Check email exists
        if (!authService.emailExists(email)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email not found!"));
        }

        authService.sendPasswordResetEmail(email);

        return ResponseEntity.ok(
            ApiResponse.success(
                "Password reset email sent! Check your inbox.", null
            )
        );
    }

    // ─── TEST ENDPOINT ─────────────────────────────────────────────────────

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(
            ApiResponse.success("API is working!", "Hello from Queue Management System!")
        );
    }

    // Temporary endpoint to generate password hash
@GetMapping("/generate-hash")
public ResponseEntity<String> generateHash(@RequestParam String password) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String hash = encoder.encode(password);
    return ResponseEntity.ok("Hash for '" + password + "': " + hash);
}
}