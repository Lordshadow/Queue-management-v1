package com.queue.management.service.impl;

import com.queue.management.dto.response.TokenNotification;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.TokenStatus;
import com.queue.management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyQueueUpdate(
            String tokenCode,
            CounterName counterName,
            TokenStatus newStatus,
            int waitingCount,
            String currentlyServing,
            String message) {

        TokenNotification notification = TokenNotification.builder()
                .tokenCode(tokenCode)
                .counterName(counterName)
                .status(newStatus)
                .waitingCount(waitingCount)
                .currentlyServing(currentlyServing)
                .message(message)
                .build();

        // Broadcast to counter-specific topic
        String destination = "/topic/queue/" + counterName.name();
        messagingTemplate.convertAndSend(destination, notification);

        log.debug("WebSocket notification sent to {}: {}", destination, message);
    }
}
