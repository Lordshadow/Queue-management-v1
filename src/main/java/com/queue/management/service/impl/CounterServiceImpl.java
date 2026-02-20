package com.queue.management.service.impl;

import com.queue.management.dto.response.QueueStatusResponse;
import com.queue.management.dto.response.TokenNotification;
import com.queue.management.entity.CounterBreakLog;
import com.queue.management.entity.DailyCounterState;
import com.queue.management.entity.ServiceCounter;
import com.queue.management.entity.Token;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.CounterStatus;
import com.queue.management.enums.NotificationType;
import com.queue.management.enums.TokenStatus;
import com.queue.management.repository.CounterBreakLogRepository;
import com.queue.management.repository.DailyCounterStateRepository;
import com.queue.management.repository.ServiceCounterRepository;
import com.queue.management.repository.TokenRepository;
import com.queue.management.service.CounterService;
import com.queue.management.service.NotificationService;
import com.queue.management.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
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
    private final DailyCounterStateRepository dailyCounterStateRepository;
    private final StatisticsService statisticsService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void startBreak(CounterName counterName, String reason, Integer estimatedDuration) {

        ServiceCounter counter = getCounter(counterName);

        if (counter.getStatus() == CounterStatus.ON_BREAK) {
            throw new RuntimeException("Counter " + counterName + " is already on break!");
        }

        counter.setStatus(CounterStatus.ON_BREAK);
        counter.setBreakStartedAt(LocalDateTime.now());
        counter.setBreakReason(reason);
        counter.setEstimatedBreakDuration(estimatedDuration);
        counterRepository.save(counter);

        CounterBreakLog breakLog = CounterBreakLog.builder()
            .counter(counter)
            .breakStart(LocalDateTime.now())
            .reason(reason)
            .estimatedDuration(estimatedDuration)
            .build();
        breakLogRepository.save(breakLog);

        log.info("Counter {} started break. Reason: {}", counterName, reason);

        // Calculate estimated resume time
        LocalDateTime estimatedResume = LocalDateTime.now().plusMinutes(
            estimatedDuration != null ? estimatedDuration : 15
        );
        String resumeTime = estimatedResume.toLocalTime().toString().substring(0, 5);

        // Broadcast break notification to all subscribers
        notificationService.notifyQueueUpdate(
            null, counterName, null,
            countWaiting(counter, LocalDate.now()),
            null,
            "Counter " + counterName + " is now on break. Resumes at " + resumeTime
        );

        // Send personal COUNTER_BREAK notification to all waiting students
        List<Token> waitingTokens = tokenRepository
            .findByCounterAndStatusAndServiceDateOrderByTokenNumberAsc(
                counter, TokenStatus.WAITING, LocalDate.now()
            );

        for (Token token : waitingTokens) {
            notificationService.notifyStudent(
                token.getStudent().getRollNumber(),
                TokenNotification.builder()
                    .type(NotificationType.COUNTER_BREAK)
                    .tokenCode(token.getTokenCode())
                    .counterName(counterName)
                    .status(TokenStatus.WAITING)
                    .waitingCount(waitingTokens.size())
                    .message("Counter " + counterName + " is now on break. Resumes at " + resumeTime)
                    .build()
            );
        }
    }

    @Override
    @Transactional
    public void endBreak(CounterName counterName) {

        ServiceCounter counter = getCounter(counterName);

        if (counter.getStatus() != CounterStatus.ON_BREAK) {
            throw new RuntimeException("Counter " + counterName + " is not on break!");
        }

        LocalDateTime breakStart = counter.getBreakStartedAt();
        LocalDateTime breakEnd = LocalDateTime.now();
        int actualDuration = (int) Duration.between(breakStart, breakEnd).toMinutes();

        counter.setStatus(CounterStatus.ACTIVE);
        counter.setBreakStartedAt(null);
        counter.setBreakReason(null);
        counter.setEstimatedBreakDuration(null);
        counterRepository.save(counter);

        List<CounterBreakLog> activeLogs = breakLogRepository.findByCounterAndBreakEndIsNull(counter);
        if (!activeLogs.isEmpty()) {
            CounterBreakLog breakLog = activeLogs.get(0);
            breakLog.setBreakEnd(breakEnd);
            breakLog.setActualDuration(actualDuration);
            breakLogRepository.save(breakLog);
        }

        log.info("Counter {} ended break. Duration: {} minutes", counterName, actualDuration);

        // Broadcast resume notification
        notificationService.notifyQueueUpdate(
            null, counterName, null,
            countWaiting(counter, LocalDate.now()),
            null,
            "Counter " + counterName + " has resumed service"
        );

        // Send personal COUNTER_RESUME to all waiting students
        List<Token> waitingTokens = tokenRepository
            .findByCounterAndStatusAndServiceDateOrderByTokenNumberAsc(
                counter, TokenStatus.WAITING, LocalDate.now()
            );

        for (Token token : waitingTokens) {
            notificationService.notifyStudent(
                token.getStudent().getRollNumber(),
                TokenNotification.builder()
                    .type(NotificationType.COUNTER_RESUME)
                    .tokenCode(token.getTokenCode())
                    .counterName(counterName)
                    .status(TokenStatus.WAITING)
                    .waitingCount(waitingTokens.size())
                    .message("Counter " + counterName + " has resumed service")
                    .build()
            );
        }
    }

    @Override
    @Transactional
    public void updateCounterStatus(CounterName counterName, CounterStatus newStatus) {
        ServiceCounter counter = getCounter(counterName);
        counter.setStatus(newStatus);
        counterRepository.save(counter);
        log.info("Counter {} status updated to: {}", counterName, newStatus);
    }

    @Override
    @Transactional
    public void updateDailyLimit(CounterName counterName, Integer newLimit) {
        if (newLimit < 1 || newLimit > 200) {
            throw new RuntimeException("Daily limit must be between 1 and 200!");
        }
        ServiceCounter counter = getCounter(counterName);
        counter.setDailyLimit(newLimit);
        counterRepository.save(counter);
        log.info("Counter {} daily limit updated to: {}", counterName, newLimit);
    }

    @Override
    public List<QueueStatusResponse> getAllCounterStatus() {
        List<QueueStatusResponse> responses = new ArrayList<>();
        for (CounterName counterName : CounterName.values()) {
            responses.add(getCounterStatus(counterName));
        }
        return responses;
    }

    @Override
    public QueueStatusResponse getCounterStatus(CounterName counterName) {

        ServiceCounter counter = getCounter(counterName);
        LocalDate today = LocalDate.now();

        Optional<Token> servingToken = tokenRepository
            .findTopByCounterAndStatusAndServiceDateOrderByServedAtDesc(counter, TokenStatus.SERVING, today);

        List<Token> allTokensToday = tokenRepository.findByCounterAndServiceDate(counter, today);

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

        // Find next WAITING token for display
        Token nextWaiting = allTokensToday.stream()
            .filter(t -> t.getStatus() == TokenStatus.WAITING)
            .min(Comparator.comparingInt(Token::getTokenNumber))
            .orElse(null);

        double avgServiceTime = statisticsService.getAverageServiceTime(counterName);
        String trend = statisticsService.getServiceTrend(counterName);
        int estimatedWaitForNew = statisticsService.getEstimatedWaitTime(counterName, waitingCount + 1);

        return QueueStatusResponse.builder()
            .counterName(counterName)
            .counterStatus(counter.getStatus())
            .currentlyServing(servingToken.map(Token::getTokenCode).orElse(null))
            .currentStudentRollNumber(servingToken.map(t -> t.getStudent().getRollNumber()).orElse(null))
            .nextTokenCode(nextWaiting != null ? nextWaiting.getTokenCode() : null)
            .nextStudentRollNumber(nextWaiting != null ? nextWaiting.getStudent().getRollNumber() : null)
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

        List<Token> waitingTokens = tokenRepository
            .findByCounterAndStatusAndServiceDateOrderByTokenNumberAsc(
                counter, TokenStatus.WAITING, today
            );

        // Reassign fresh sequential numbers starting from 1
        int newNumber = 1;
        for (Token token : waitingTokens) {
            token.setStatus(TokenStatus.RESCHEDULED);
            token.setIsRescheduled(true);
            token.setOriginalServiceDate(today);
            token.setServiceDate(tomorrow);
            token.setTokenNumber(newNumber);
            token.setTokenCode(counterName.name() + "-" + String.format("%03d", newNumber));
            tokenRepository.save(token);
            newNumber++;
        }

        // Set tomorrow's DailyCounterState so new tokens continue after the rescheduled ones
        DailyCounterState tomorrowState = dailyCounterStateRepository
            .findByCounterAndServiceDate(counter, tomorrow)
            .orElseGet(() -> DailyCounterState.builder()
                .counter(counter)
                .serviceDate(tomorrow)
                .lastTokenNumber(0)
                .build());
        tomorrowState.setLastTokenNumber(waitingTokens.size());
        dailyCounterStateRepository.save(tomorrowState);

        counter.setStatus(CounterStatus.CLOSED);
        counterRepository.save(counter);

        log.info("Counter {} stopped. {} tokens rescheduled to {} with fresh numbers",
            counterName, waitingTokens.size(), tomorrow);
    }

    @Override
    @Transactional
    public void stopAndExpire(CounterName counterName) {

        ServiceCounter counter = getCounter(counterName);
        LocalDate today = LocalDate.now();

        List<Token> waitingTokens = tokenRepository
            .findByCounterAndStatusAndServiceDateOrderByTokenNumberAsc(
                counter, TokenStatus.WAITING, today
            );

        for (Token token : waitingTokens) {
            token.setStatus(TokenStatus.DROPPED);
            tokenRepository.save(token);
        }

        counter.setStatus(CounterStatus.CLOSED);
        counterRepository.save(counter);

        log.info("Counter {} stopped. {} tokens expired", counterName, waitingTokens.size());
    }

    private ServiceCounter getCounter(CounterName counterName) {
        return counterRepository
            .findByName(counterName)
            .orElseThrow(() -> new RuntimeException("Counter not found: " + counterName));
    }

    private int countWaiting(ServiceCounter counter, LocalDate date) {
        return tokenRepository.findByCounterAndStatusAndServiceDateOrderByTokenNumberAsc(
            counter, TokenStatus.WAITING, date
        ).size();
    }
}
