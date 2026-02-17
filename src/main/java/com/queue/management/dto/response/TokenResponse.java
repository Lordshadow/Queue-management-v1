package com.queue.management.dto.response;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.TokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    // Token ID
    private Long id;

    // Token code (e.g., "A-045")
    private String tokenCode;

    // Token number (e.g., 45)
    private Integer tokenNumber;

    // Which counter (A or B)
    private CounterName counterName;

    // Current status
    private TokenStatus status;

    // Service date
    private LocalDate serviceDate;

    // Position in queue (how many ahead)
    private Integer position;

    // Estimated wait time in minutes
    private Integer estimatedWaitTime;

    // When token was created
    private LocalDateTime createdAt;

    // When token started being served
    private LocalDateTime servedAt;

    // When token was completed
    private LocalDateTime completedAt;

    // Is this a rescheduled token?
    private Boolean isRescheduled;
}