package com.queue.management.service;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.TokenStatus;

public interface NotificationService {

    /**
     * Broadcast a token status change to subscribers of /topic/queue/{counterName}.
     *
     * @param tokenCode     the token that changed (e.g., "A-007")
     * @param counterName   the counter the token belongs to
     * @param newStatus     the new status of the token
     * @param waitingCount  current number of WAITING tokens at this counter
     * @param currentlyServing  token code currently being served, or null if idle
     * @param message       human-readable event description
     */
    void notifyQueueUpdate(
            String tokenCode,
            CounterName counterName,
            TokenStatus newStatus,
            int waitingCount,
            String currentlyServing,
            String message
    );
}
