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
public class TokenHistoryResponse {

    private Long id;
    private String tokenCode;
    private CounterName counterName;
    private TokenStatus status;
    private LocalDate serviceDate;
    private LocalDateTime createdAt;
    private LocalDateTime servedAt;
    private LocalDateTime completedAt;
    private Boolean isRescheduled;

    // Minutes from token generation to being called (null if never served)
    private Long waitTimeMinutes;

    // Minutes from being called to completion (null if not completed)
    private Long serviceTimeMinutes;
}
