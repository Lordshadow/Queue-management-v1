package com.queue.management.enums;

public enum NotificationType {
    QUEUE_UPDATE,         // General queue change broadcast
    YOUR_TURN,            // Student is next to be called
    POSITION_ALERT,       // Student entered the "next 4" zone
    TOKEN_COMPLETED_AHEAD,// A token ahead of you completed
    COUNTER_BREAK,        // Your counter went on break
    COUNTER_RESUME        // Your counter resumed from break
}
