package com.queue.management.service;

import com.queue.management.dto.response.QueueStatusResponse;
import com.queue.management.enums.CounterName;
import java.util.List;

public interface QueueService {

    // Get status of all counters
    // Used by students to see queue status
    List<QueueStatusResponse> getQueueStatus();

    // Get status of specific counter
    QueueStatusResponse getCounterQueueStatus(CounterName counterName);

    // Get total waiting count across all counters
    int getTotalWaitingCount();

    // Check if any counter is available
    boolean isAnyCounterAvailable();
}