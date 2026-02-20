package com.queue.management.service;

import com.queue.management.dto.response.TokenHistoryResponse;
import com.queue.management.dto.response.TokenResponse;
import com.queue.management.enums.CounterName;
import java.util.List;

public interface TokenService {

    // Generate a new token for student
    TokenResponse generateToken(String rollNumber);

    // Get student's current active token (WAITING, SERVING, or RESCHEDULED)
    TokenResponse getMyToken(String rollNumber);

    // Cancel student's WAITING or SERVING token
    void cancelToken(String rollNumber);

    // Call next WAITING token for a counter
    TokenResponse callNextToken(CounterName counterName);

    // Mark current token as COMPLETED (auto-calls next)
    TokenResponse completeToken(CounterName counterName);

    // Mark current token as DROPPED (auto-calls next)
    TokenResponse dropToken(CounterName counterName);

    // Get position of student in queue
    int getQueuePosition(String rollNumber);

    // Get full token history for a student (all past tokens, newest first)
    List<TokenHistoryResponse> getTokenHistory(String rollNumber);
}
