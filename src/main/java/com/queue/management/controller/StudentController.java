package com.queue.management.controller;

import com.queue.management.dto.request.ChangePasswordRequest;
import com.queue.management.dto.request.UpdateProfileRequest;
import com.queue.management.dto.response.ApiResponse;
import com.queue.management.dto.response.ProfileResponse;
import com.queue.management.dto.response.QueueStatusResponse;
import com.queue.management.dto.response.TokenHistoryResponse;
import com.queue.management.dto.response.TokenResponse;
import com.queue.management.enums.UserType;
import com.queue.management.security.SecurityUser;
import com.queue.management.service.AuthService;
import com.queue.management.service.QueueService;
import com.queue.management.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final TokenService tokenService;
    private final QueueService queueService;
    private final AuthService authService;

    // ─── TOKEN MANAGEMENT ──────────────────────────────────────────────────

    @PostMapping("/tokens/generate")
    public ResponseEntity<ApiResponse<TokenResponse>> generateToken(
            @AuthenticationPrincipal SecurityUser user) {

        TokenResponse token = tokenService.generateToken(user.getUsername());
        log.info("Token generated for student: {}", user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Token generated successfully!", token));
    }

    @GetMapping("/tokens/my-token")
    public ResponseEntity<ApiResponse<TokenResponse>> getMyToken(
            @AuthenticationPrincipal SecurityUser user) {

        TokenResponse token = tokenService.getMyToken(user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Token fetched successfully!", token));
    }

    @DeleteMapping("/tokens/cancel")
    public ResponseEntity<ApiResponse<String>> cancelToken(
            @AuthenticationPrincipal SecurityUser user) {

        tokenService.cancelToken(user.getUsername());
        log.info("Token cancelled by student: {}", user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Token cancelled successfully!", null));
    }

    @GetMapping("/tokens/position")
    public ResponseEntity<ApiResponse<Integer>> getPosition(
            @AuthenticationPrincipal SecurityUser user) {

        int position = tokenService.getQueuePosition(user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Position fetched!", position));
    }

    @GetMapping("/tokens/history")
    public ResponseEntity<ApiResponse<List<TokenHistoryResponse>>> getTokenHistory(
            @AuthenticationPrincipal SecurityUser user) {

        List<TokenHistoryResponse> history = tokenService.getTokenHistory(user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Token history fetched!", history));
    }

    // ─── QUEUE STATUS ──────────────────────────────────────────────────────

    @GetMapping("/queue/status")
    public ResponseEntity<ApiResponse<List<QueueStatusResponse>>> getQueueStatus() {

        List<QueueStatusResponse> status = queueService.getQueueStatus();
        return ResponseEntity.ok(ApiResponse.success("Queue status fetched!", status));
    }

    @GetMapping("/queue/waiting-count")
    public ResponseEntity<ApiResponse<Integer>> getTotalWaiting() {

        int count = queueService.getTotalWaitingCount();
        return ResponseEntity.ok(ApiResponse.success("Total waiting count!", count));
    }

    // ─── ACCOUNT MANAGEMENT ───────────────────────────────────────────────

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @AuthenticationPrincipal SecurityUser user) {

        ProfileResponse profile = authService.getStudentProfile(user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched!", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<String>> updateProfile(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody UpdateProfileRequest request) {

        authService.updateStudentProfile(user.getUsername(), request.getName(), request.getEmail());
        log.info("Profile updated for student: {}", user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully!", null));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody ChangePasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("New password and confirm password do not match!"));
        }

        authService.changePassword(
            user.getUsername(),
            request.getCurrentPassword(),
            request.getNewPassword(),
            UserType.STUDENT
        );
        log.info("Password changed for student: {}", user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully!", null));
    }
}
