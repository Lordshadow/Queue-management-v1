package com.queue.management.dto.response;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.TokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenNotification {

    // Which token this event is about (e.g., "A-007")
    private String tokenCode;

    // Which counter triggered this event
    private CounterName counterName;

    // New status of the token
    private TokenStatus status;

    // How many tokens are now waiting at this counter
    private int waitingCount;

    // Token code currently being served (null if idle)
    private String currentlyServing;

    // Human-readable description (e.g., "Counter A is now serving A-007")
    private String message;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
