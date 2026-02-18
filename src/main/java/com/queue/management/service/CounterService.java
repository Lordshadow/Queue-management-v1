package com.queue.management.service;

import com.queue.management.dto.response.QueueStatusResponse;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.CounterStatus;
import java.util.List;

public interface CounterService {

    // Start a break for a counter
    // Staff must provide reason and estimated duration
    void startBreak(CounterName counterName,
                   String reason,
                   Integer estimatedDuration);

    // End break for a counter
    // Counter goes back to ACTIVE
    void endBreak(CounterName counterName);

    // Update counter status manually
    void updateCounterStatus(CounterName counterName,
                            CounterStatus newStatus);

    // Update daily token limit for a counter
    void updateDailyLimit(CounterName counterName,
                         Integer newLimit);

    // Get status of both counters
    List<QueueStatusResponse> getAllCounterStatus();

    // Get status of one specific counter
    QueueStatusResponse getCounterStatus(CounterName counterName);

    // Stop queue and reschedule all WAITING tokens to tomorrow
    void stopAndReschedule(CounterName counterName);

    // Stop queue and expire all WAITING tokens
    void stopAndExpire(CounterName counterName);
}