package com.queue.management.controller;

import com.queue.management.dto.response.ApiResponse;
import com.queue.management.dto.response.QueueStatusResponse;
import com.queue.management.dto.response.TokenResponse;
import com.queue.management.security.SecurityUser;
import com.queue.management.service.CounterService;
import com.queue.management.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counter")
@RequiredArgsConstructor
@Slf4j
public class CounterController {

    private final TokenService tokenService;
    private final CounterService counterService;

    // ─── TOKEN MANAGEMENT ──────────────────────────────────────────────────

    // Call next token
    @PostMapping("/tokens/call-next")
    public ResponseEntity<ApiResponse<TokenResponse>> callNext(
            @AuthenticationPrincipal SecurityUser user) {

        TokenResponse token = tokenService
            .callNextToken(user.getAssignedCounter());

        log.info("Called next token for Counter: {}",
            user.getAssignedCounter());
        return ResponseEntity.ok(
            ApiResponse.success("Next token called!", token)
        );
    }

    // Complete current token
    @PostMapping("/tokens/complete")
    public ResponseEntity<ApiResponse<TokenResponse>> completeToken(
            @AuthenticationPrincipal SecurityUser user) {

        TokenResponse token = tokenService
            .completeToken(user.getAssignedCounter());

        log.info("Token completed at Counter: {}",
            user.getAssignedCounter());
        return ResponseEntity.ok(
            ApiResponse.success("Token completed!", token)
        );
    }

    // Drop current token
    @PostMapping("/tokens/drop")
    public ResponseEntity<ApiResponse<TokenResponse>> dropToken(
            @AuthenticationPrincipal SecurityUser user) {

        TokenResponse token = tokenService
            .dropToken(user.getAssignedCounter());

        log.info("Token dropped at Counter: {}",
            user.getAssignedCounter());
        return ResponseEntity.ok(
            ApiResponse.success("Token dropped!", token)
        );
    }

    // ─── BREAK MANAGEMENT ──────────────────────────────────────────────────

    // Start break
    @PostMapping("/break/start")
    public ResponseEntity<ApiResponse<String>> startBreak(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam String reason,
            @RequestParam Integer estimatedDuration) {

        counterService.startBreak(
            user.getAssignedCounter(),
            reason,
            estimatedDuration
        );

        log.info("Break started at Counter: {}",
            user.getAssignedCounter());
        return ResponseEntity.ok(
            ApiResponse.success("Break started!", null)
        );
    }

    // End break
    @PostMapping("/break/end")
    public ResponseEntity<ApiResponse<String>> endBreak(
            @AuthenticationPrincipal SecurityUser user) {

        counterService.endBreak(user.getAssignedCounter());

        log.info("Break ended at Counter: {}",
            user.getAssignedCounter());
        return ResponseEntity.ok(
            ApiResponse.success("Break ended! Counter is now active.", null)
        );
    }

    // ─── QUEUE OPERATIONS ──────────────────────────────────────────────────

    // Stop queue and reschedule tokens
    @PostMapping("/queue/stop-reschedule")
    public ResponseEntity<ApiResponse<String>> stopAndReschedule(
            @AuthenticationPrincipal SecurityUser user) {

        counterService.stopAndReschedule(user.getAssignedCounter());

        return ResponseEntity.ok(
            ApiResponse.success(
                "Queue stopped! Waiting tokens rescheduled to tomorrow.",
                null
            )
        );
    }

    // Stop queue and expire tokens
    @PostMapping("/queue/stop-expire")
    public ResponseEntity<ApiResponse<String>> stopAndExpire(
            @AuthenticationPrincipal SecurityUser user) {

        counterService.stopAndExpire(user.getAssignedCounter());

        return ResponseEntity.ok(
            ApiResponse.success(
                "Queue stopped! All waiting tokens expired.",
                null
            )
        );
    }

    // ─── COUNTER STATUS ────────────────────────────────────────────────────

    // Get my counter status
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<QueueStatusResponse>> getMyStatus(
            @AuthenticationPrincipal SecurityUser user) {

        QueueStatusResponse status = counterService
            .getCounterStatus(user.getAssignedCounter());

        return ResponseEntity.ok(
            ApiResponse.success("Counter status fetched!", status)
        );
    }

    // Update daily limit
    @PutMapping("/config/daily-limit")
    public ResponseEntity<ApiResponse<String>> updateDailyLimit(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam Integer newLimit) {

        counterService.updateDailyLimit(
            user.getAssignedCounter(),
            newLimit
        );

        return ResponseEntity.ok(
            ApiResponse.success(
                "Daily limit updated to: " + newLimit, null
            )
        );
    }
}