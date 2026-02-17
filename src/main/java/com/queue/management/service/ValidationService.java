package com.queue.management.service;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.TokenStatus;

public interface ValidationService {

    // Check if current time is within working hours
    // Working hours: 9:20 AM - 4:30 PM
    // Break time: 2:00 PM - 2:45 PM
    boolean isWithinWorkingHours();

    // Check if student can generate a token today
    // Rules:
    // - Must be within working hours
    // - Student must not have active token (WAITING or SERVING)
    // - At least one counter must be available
    boolean canStudentGenerateToken(String rollNumber);

    // Check if a specific counter can accept new tokens
    // Rules:
    // - Counter must be ACTIVE
    // - Counter must not have reached daily limit
    boolean isCounterAcceptingTokens(CounterName counterName);

    // Check if token status transition is valid
    // Valid transitions:
    // WAITING → SERVING
    // SERVING → COMPLETED
    // SERVING → DROPPED
    // WAITING → RESCHEDULED
    boolean isValidStatusTransition(TokenStatus currentStatus, TokenStatus newStatus);

    // Check if it is break time
    // Break time: 2:00 PM - 2:45 PM
    boolean isBreakTime();
}