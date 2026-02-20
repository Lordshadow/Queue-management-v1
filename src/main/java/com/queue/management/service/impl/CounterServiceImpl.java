package com.queue.management.service.impl;

import com.queue.management.dto.response.QueueStatusResponse;
import com.queue.management.entity.CounterBreakLog;
import com.queue.management.entity.ServiceCounter;
import com.queue.management.entity.Token;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.CounterStatus;
import com.queue.management.enums.TokenStatus;
import com.queue.management.repository.CounterBreakLogRepository;
import com.queue.management.repository.ServiceCounterRepository;
import com.queue.management.repository.TokenRepository;
import com.queue.management.service.CounterService;
import com.queue.management.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CounterServiceImpl implements CounterService {

    private final ServiceCounterRepository counterRepository;
    private final TokenRepository tokenRepository;
    private final CounterBreakLogRepository breakLogRepository;
    private final StatisticsService statisticsService;

    @Override
    @Transactional
    public void startBreak(CounterName counterName,
                          String reason,
                          Integer estimatedDuration) {

        // Get counter
        ServiceCounter counter = getCounter(counterName);

        // Check if already on break
        if (counter.getStatus() == CounterStatus.ON_BREAK) {
            throw new RuntimeException(
                "Counter " + counterName + " is already on break!"
            );
        }

        // Update counter status
        counter.setStatus(CounterStatus.ON_BREAK);
        counter.setBreakStartedAt(LocalDateTime.now());
        counter.setBreakReason(reason);
        counter.setEstimatedBreakDuration(estimatedDuration);
        counterRepository.save(counter);

        // Create break log entry
        CounterBreakLog breakLog = CounterBreakLog.builder()
            .counter(counter)
            .breakStart(LocalDateTime.now())
            .reason(reason)
            .estimatedDuration(estimatedDuration)
            .build();
        breakLogRepository.save(breakLog);

        log.info("Counter {} started break. Reason: {}", counterName, reason);
    }

    @Override
    @Transactional
    public void endBreak(CounterName counterName) {

        // Get counter
        ServiceCounter counter = getCounter(counterName);

        // Check if actually on break
        if (counter.getStatus() != CounterStatus.ON_BREAK) {
            throw new RuntimeException(
                "Counter " + counterName + " is not on break!"
            );
        }

        // Calculate actual break duration
        LocalDateTime breakStart = counter.getBreakStartedAt();
        LocalDateTime breakEnd = LocalDateTime.now();
        int actualDuration = (int) Duration.between(breakStart, breakEnd)
            .toMinutes();

        // Update counter status back to ACTIVE
        counter.setStatus(CounterStatus.ACTIVE);
        counter.setBreakStartedAt(null);
        counter.setBreakReason(null);
        counter.setEstimatedBreakDuration(null);
        counterRepository.save(counter);

        // Update break log
        List<CounterBreakLog> activeLogs = breakLogRepository
            .findByCounterAndBreakEndIsNull(counter);

        if (!activeLogs.isEmpty()) {
            CounterBreakLog breakLog = activeLogs.get(0);
            breakLog.setBreakEnd(breakEnd);
            breakLog.setActualDuration(actualDuration);
            breakLogRepository.save(breakLog);
        }

        log.info("Counter {} ended break. Duration: {} minutes",
            counterName, actualDuration);
    }

    @Override
    @Transactional
    public void updateCounterStatus(CounterName counterName,
                                   CounterStatus newStatus) {

        ServiceCounter counter = getCounter(counterName);
        counter.setStatus(newStatus);
        counterRepository.save(counter);

        log.info("Counter {} status updated to: {}", counterName, newStatus);
    }

    @Override
    @Transactional
    public void updateDailyLimit(CounterName counterName,
                                Integer newLimit) {

        // Validate limit
        if (newLimit < 1 || newLimit > 200) {
            throw new RuntimeException(
                "Daily limit must be between 1 and 200!"
            );
        }

        ServiceCounter counter = getCounter(counterName);
        counter.setDailyLimit(newLimit);
        counterRepository.save(counter);

        log.info("Counter {} daily limit updated to: {}",
            counterName, newLimit);
    }

    @Override
    public List<QueueStatusResponse> getAllCounterStatus() {

        List<QueueStatusResponse> responses = new ArrayList<>();

        // Get status for both counters
        for (CounterName counterName : CounterName.values()) {
            responses.add(getCounterStatus(counterName));
        }

        return responses;
    }

    @Override
    public QueueStatusResponse getCounterStatus(CounterName counterName) {

        ServiceCounter counter = getCounter(counterName);
        LocalDate today = LocalDate.now();

        // Get currently serving token
        Optional<Token> servingToken = tokenRepository
            .findByCounterAndStatus(counter, TokenStatus.SERVING);

        // Count tokens by status
        List<Token> allTokensToday = tokenRepository
            .findByCounterAndServiceDate(counter, today);

        int waitingCount = 0;
        int completedCount = 0;
        int droppedCount = 0;

        for (Token token : allTokensToday) {
            switch (token.getStatus()) {
                case WAITING -> waitingCount++;
                case COMPLETED -> completedCount++;
                case DROPPED -> droppedCount++;
                default -> {}
            }
        }

        // Get statistics
        double avgServiceTime = statisticsService
            .getAverageServiceTime(counterName);
        String trend = statisticsService.getServiceTrend(counterName);
        int estimatedWaitForNew = statisticsService
            .getEstimatedWaitTime(counterName, waitingCount + 1);

        return QueueStatusResponse.builder()
            .counterName(counterName)
            .counterStatus(counter.getStatus())
            .currentlyServing(servingToken
                .map(Token::getTokenCode)
                .orElse(null))
            .waitingCount(waitingCount)
            .completedCount(completedCount)
            .droppedCount(droppedCount)
            .totalIssuedToday(allTokensToday.size())
            .dailyLimit(counter.getDailyLimit())
            .averageServiceTime(avgServiceTime)
            .serviceTrend(trend)
            .estimatedWaitTimeForNew(estimatedWaitForNew)
            .onBreak(counter.getStatus() == CounterStatus.ON_BREAK)
            .breakReason(counter.getBreakReason())
            .estimatedBreakDuration(counter.getEstimatedBreakDuration())
            .build();
    }

    @Override
    @Transactional
    public void stopAndReschedule(CounterName counterName) {

        ServiceCounter counter = getCounter(counterName);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // Get all WAITING tokens for this counter today
        List<Token> waitingTokens = tokenRepository
            .findByCounterAndStatusOrderByTokenNumberAsc(
                counter, TokenStatus.WAITING
            );

        // Reschedule each token
        for (Token token : waitingTokens) {
            token.setStatus(TokenStatus.RESCHEDULED);
            token.setIsRescheduled(true);
            token.setOriginalServiceDate(today);
            token.setServiceDate(tomorrow);
            tokenRepository.save(token);
        }

        // Close the counter
        counter.setStatus(CounterStatus.CLOSED);
        counterRepository.save(counter);

        log.info("Counter {} stopped. {} tokens rescheduled to {}",
            counterName, waitingTokens.size(), tomorrow);
    }

    @Override
    @Transactional
    public void stopAndExpire(CounterName counterName) {

        ServiceCounter counter = getCounter(counterName);

        // Get all WAITING tokens for this counter today
        List<Token> waitingTokens = tokenRepository
            .findByCounterAndStatusOrderByTokenNumberAsc(
                counter, TokenStatus.WAITING
            );

        // Drop all waiting tokens
        for (Token token : waitingTokens) {
            token.setStatus(TokenStatus.DROPPED);
            tokenRepository.save(token);
        }

        // Close the counter
        counter.setStatus(CounterStatus.CLOSED);
        counterRepository.save(counter);

        log.info("Counter {} stopped. {} tokens expired",
            counterName, waitingTokens.size());
    }

    // Helper method to get counter
    private ServiceCounter getCounter(CounterName counterName) {
        return counterRepository
            .findByName(counterName)
            .orElseThrow(() -> new RuntimeException(
                "Counter not found: " + counterName
            ));
    }
}