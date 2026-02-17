package com.queue.management.dto.response;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.CounterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {

    // Counter name (A or B)
    private CounterName counterName;

    // Counter status (ACTIVE, ON_BREAK, etc.)
    private CounterStatus counterStatus;

    // Currently serving token code (e.g., "A-033")
    private String currentlyServing;

    // How many tokens are WAITING
    private Integer waitingCount;

    // How many tokens COMPLETED today
    private Integer completedCount;

    // How many tokens DROPPED today
    private Integer droppedCount;

    // Total tokens issued today
    private Integer totalIssuedToday;

    // Daily limit for this counter
    private Integer dailyLimit;

    // Average service time in minutes
    private Double averageServiceTime;

    // Service trend (FASTER, SLOWER, STABLE)
    private String serviceTrend;

    // Estimated wait time for NEW student (in minutes)
    private Integer estimatedWaitTimeForNew;

    // Is counter on break?
    private Boolean onBreak;

    // Break reason (if on break)
    private String breakReason;

    // Estimated break end time (if on break)
    private Integer estimatedBreakDuration;
}