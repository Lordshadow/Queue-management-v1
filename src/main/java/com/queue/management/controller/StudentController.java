package com.queue.management.controller;

import com.queue.management.dto.response.ApiResponse;
import com.queue.management.dto.response.QueueStatusResponse;
import com.queue.management.dto.response.TokenResponse;
import com.queue.management.security.SecurityUser;
import com.queue.management.service.QueueService;
import com.queue.management.service.TokenService;
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

    // ─── TOKEN MANAGEMENT ──────────────────────────────────────────────────

    // Generate new token
    @PostMapping("/tokens/generate")
    public ResponseEntity<ApiResponse<TokenResponse>> generateToken(
            @AuthenticationPrincipal SecurityUser user) {

        TokenResponse token = tokenService.generateToken(user.getUsername());

        log.info("Token generated for student: {}", user.getUsername());
        return ResponseEntity.ok(
            ApiResponse.success("Token generated successfully!", token)
        );
    }

    // Get my current token
    @GetMapping("/tokens/my-token")
    public ResponseEntity<ApiResponse<TokenResponse>> getMyToken(
            @AuthenticationPrincipal SecurityUser user) {

        TokenResponse token = tokenService.getMyToken(user.getUsername());

        return ResponseEntity.ok(
            ApiResponse.success("Token fetched successfully!", token)
        );
    }

    // Cancel my token
    @DeleteMapping("/tokens/cancel")
    public ResponseEntity<ApiResponse<String>> cancelToken(
            @AuthenticationPrincipal SecurityUser user) {

        tokenService.cancelToken(user.getUsername());

        log.info("Token cancelled by student: {}", user.getUsername());
        return ResponseEntity.ok(
            ApiResponse.success("Token cancelled successfully!", null)
        );
    }

    // Get my position in queue
    @GetMapping("/tokens/position")
    public ResponseEntity<ApiResponse<Integer>> getPosition(
            @AuthenticationPrincipal SecurityUser user) {

        int position = tokenService.getQueuePosition(user.getUsername());

        return ResponseEntity.ok(
            ApiResponse.success("Position fetched!", position)
        );
    }

    // ─── QUEUE STATUS ──────────────────────────────────────────────────────

    // Get status of all counters
    @GetMapping("/queue/status")
    public ResponseEntity<ApiResponse<List<QueueStatusResponse>>> getQueueStatus() {

        List<QueueStatusResponse> status = queueService.getQueueStatus();

        return ResponseEntity.ok(
            ApiResponse.success("Queue status fetched!", status)
        );
    }

    // Get total waiting count
    @GetMapping("/queue/waiting-count")
    public ResponseEntity<ApiResponse<Integer>> getTotalWaiting() {

        int count = queueService.getTotalWaitingCount();

        return ResponseEntity.ok(
            ApiResponse.success("Total waiting count!", count)
        );
    }
}