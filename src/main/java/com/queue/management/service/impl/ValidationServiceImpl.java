package com.queue.management.service.impl;

import com.queue.management.entity.ServiceCounter;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.CounterStatus;
import com.queue.management.enums.TokenStatus;
import com.queue.management.repository.ServiceCounterRepository;
import com.queue.management.repository.TokenRepository;
import com.queue.management.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    // Inject repositories
    private final ServiceCounterRepository counterRepository;
    private final TokenRepository tokenRepository;

    // Working hours constants
    private static final LocalTime COUNTER_START = LocalTime.of(9, 20);   // 9:20 AM
    private static final LocalTime COUNTER_END = LocalTime.of(16, 30);    // 4:30 PM
    private static final LocalTime BREAK_START = LocalTime.of(14, 0);     // 2:00 PM
    private static final LocalTime BREAK_END = LocalTime.of(14, 45);      // 2:45 PM

    @Override
    public boolean isWithinWorkingHours() {
        LocalTime now = LocalTime.now();

        // Check if within working hours
        if (now.isBefore(COUNTER_START) || now.isAfter(COUNTER_END)) {
            return false;
        }

        // Check if break time
        if (isBreakTime()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isBreakTime() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(BREAK_START) && !now.isAfter(BREAK_END);
    }

    @Override
    public boolean canStudentGenerateToken(String rollNumber) {

        // Check working hours first
        if (!isWithinWorkingHours()) {
            return false;
        }

        // Block if student already has a WAITING or SERVING token today
        boolean hasActiveToken = tokenRepository.existsByStudent_RollNumberAndStatusInAndServiceDate(
            rollNumber,
            EnumSet.of(TokenStatus.WAITING, TokenStatus.SERVING),
            LocalDate.now()
        );

        return !hasActiveToken;
    }

    @Override
    public boolean isCounterAcceptingTokens(CounterName counterName) {

        // Find the counter
        Optional<ServiceCounter> counterOpt = counterRepository.findByName(counterName);

        if (counterOpt.isEmpty()) {
            return false;
        }

        ServiceCounter counter = counterOpt.get();

        // Check counter status
        if (counter.getStatus() != CounterStatus.ACTIVE) {
            return false;
        }

        // Check daily limit
        long tokensToday = tokenRepository
            .countByCounterAndServiceDate(counter, LocalDate.now());

        if (tokensToday >= counter.getDailyLimit()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isValidStatusTransition(TokenStatus currentStatus, TokenStatus newStatus) {

        // Define valid transitions
        switch (currentStatus) {
            case WAITING:
                // WAITING can go to SERVING or RESCHEDULED
                return newStatus == TokenStatus.SERVING
                    || newStatus == TokenStatus.RESCHEDULED
                    || newStatus == TokenStatus.DROPPED;

            case SERVING:
                // SERVING can go to COMPLETED or DROPPED
                return newStatus == TokenStatus.COMPLETED
                    || newStatus == TokenStatus.DROPPED;

            case COMPLETED:
                // COMPLETED is final - no transitions
                return false;

            case DROPPED:
                // DROPPED is final - no transitions
                return false;

            case RESCHEDULED:
                // RESCHEDULED can go back to WAITING (next day)
                return newStatus == TokenStatus.WAITING;

            default:
                return false;
        }
    }
}