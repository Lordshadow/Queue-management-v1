package com.queue.management.service.impl;

import com.queue.management.dto.response.QueueStatusResponse;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.CounterStatus;
import com.queue.management.service.CounterService;
import com.queue.management.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueServiceImpl implements QueueService {

    private final CounterService counterService;

    @Override
    public List<QueueStatusResponse> getQueueStatus() {
        return counterService.getAllCounterStatus();
    }

    @Override
    public QueueStatusResponse getCounterQueueStatus(CounterName counterName) {
        return counterService.getCounterStatus(counterName);
    }

    @Override
    public int getTotalWaitingCount() {
        List<QueueStatusResponse> allStatus = counterService.getAllCounterStatus();

        int total = 0;
        for (QueueStatusResponse status : allStatus) {
            total += status.getWaitingCount();
        }

        log.debug("Total waiting count: {}", total);
        return total;
    }

    @Override
    public boolean isAnyCounterAvailable() {
        List<QueueStatusResponse> allStatus = counterService.getAllCounterStatus();

        for (QueueStatusResponse status : allStatus) {
            if (status.getCounterStatus() == CounterStatus.ACTIVE) {
                return true;
            }
        }

        return false;
    }
}