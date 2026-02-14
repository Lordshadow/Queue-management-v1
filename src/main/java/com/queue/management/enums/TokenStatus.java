package com.queue.management.enums;

public enum TokenStatus {
    WAITING,      // Token just created, in queue
    SERVING,      // Student is currently at counter
    COMPLETED,    // Service finished successfully
    DROPPED,      // Student didn't show up or cancelled
    RESCHEDULED   // Moved to next day
}
