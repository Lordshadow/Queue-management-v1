package com.queue.management.dto.response;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.NotificationType;
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

    // Type of notification for frontend routing
    @Builder.Default
    private NotificationType type = NotificationType.QUEUE_UPDATE;

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

    // Student's current position in queue (0 = next to be called)
    private Integer yourPosition;

    // Human-readable description
    private String message;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
