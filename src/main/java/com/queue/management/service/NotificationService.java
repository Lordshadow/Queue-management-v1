package com.queue.management.service;

import com.queue.management.dto.response.TokenNotification;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.TokenStatus;

public interface NotificationService {

    /**
     * Broadcast a token status change to all subscribers of /topic/queue/{counterName}.
     * Used for general queue-level events (new token, call-next, complete, drop).
     */
    void notifyQueueUpdate(
            String tokenCode,
            CounterName counterName,
            TokenStatus newStatus,
            int waitingCount,
            String currentlyServing,
            String message
    );

    /**
     * Send a personal notification to a specific student's topic /topic/student/{rollNumber}.
     * Used for position alerts, your-turn, break/resume alerts.
     */
    void notifyStudent(String rollNumber, TokenNotification notification);
}
