package com.queue.management.service;

import com.queue.management.dto.response.TokenResponse;
import com.queue.management.enums.CounterName;

public interface TokenService {

    // Generate a new token for student
    // Returns token details
    TokenResponse generateToken(String rollNumber);

    // Get student's current token for today
    TokenResponse getMyToken(String rollNumber);

    // Cancel/drop student's token
    void cancelToken(String rollNumber);

    // Call next WAITING token for a counter
    // Called by counter staff
    TokenResponse callNextToken(CounterName counterName);

    // Mark current token as COMPLETED
    // Called by counter staff
    TokenResponse completeToken(CounterName counterName);

    // Mark current token as DROPPED
    // Called by counter staff
    TokenResponse dropToken(CounterName counterName);

    // Get position of student in queue
    // Returns how many tokens are ahead
    int getQueuePosition(String rollNumber);
}