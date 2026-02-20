package com.queue.management.service.impl;

import com.queue.management.dto.response.TokenNotification;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.NotificationType;
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
                .type(NotificationType.QUEUE_UPDATE)
                .tokenCode(tokenCode)
                .counterName(counterName)
                .status(newStatus)
                .waitingCount(waitingCount)
                .currentlyServing(currentlyServing)
                .message(message)
                .build();

        String destination = "/topic/queue/" + counterName.name();
        messagingTemplate.convertAndSend(destination, notification);

        log.debug("Queue notification → {}: {}", destination, message);
    }

    @Override
    public void notifyStudent(String rollNumber, TokenNotification notification) {
        String destination = "/topic/student/" + rollNumber;
        messagingTemplate.convertAndSend(destination, notification);

        log.debug("Student notification → {}: {}", destination, notification.getMessage());
    }
}
