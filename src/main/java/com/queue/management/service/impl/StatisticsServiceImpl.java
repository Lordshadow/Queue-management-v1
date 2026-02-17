package com.queue.management.service.impl;

import com.queue.management.entity.ServiceCounter;
import com.queue.management.entity.Token;
import com.queue.management.enums.CounterName;
import com.queue.management.repository.ServiceCounterRepository;
import com.queue.management.repository.TokenRepository;
import com.queue.management.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private final ServiceCounterRepository counterRepository;
    private final TokenRepository tokenRepository;

    // Default service time if no data available
    private static final double DEFAULT_SERVICE_TIME = 5.0;



    @Override
    public double getAverageServiceTime(CounterName counterName) {
        try {
            // Get counter
            ServiceCounter counter = counterRepository
                .findByName(counterName)
                .orElse(null);

            if (counter == null) {
                return DEFAULT_SERVICE_TIME;
            }

            // Get last 10 completed tokens
            List<Token> completedTokens = tokenRepository
                .findLast10CompletedTokens(counter, LocalDate.now());

            // If no completed tokens yet, return default
            if (completedTokens.isEmpty()) {
                return DEFAULT_SERVICE_TIME;
            }

            // Calculate average time between servedAt and completedAt
            double totalMinutes = 0;
            int validCount = 0;

            for (Token token : completedTokens) {
                if (token.getServedAt() != null && token.getCompletedAt() != null) {
                    // Calculate duration in minutes
                    Duration duration = Duration.between(
                        token.getServedAt(),
                        token.getCompletedAt()
                    );
                    totalMinutes += duration.toMinutes();
                    validCount++;
                }
            }

            // Return average (or default if no valid data)
            if (validCount == 0) {
                return DEFAULT_SERVICE_TIME;
            }

            double average = totalMinutes / validCount;
            log.debug("Average service time for Counter {}: {} minutes", counterName, average);
            return average;

        } catch (Exception e) {
            log.error("Error calculating average service time for Counter {}", counterName, e);
            return DEFAULT_SERVICE_TIME;
        }
    }

    @Override
    public int getEstimatedWaitTime(CounterName counterName, int position) {
        try {
            // Get average service time
            double avgServiceTime = getAverageServiceTime(counterName);

            // Calculate: position × average service time
            int estimatedMinutes = (int) Math.ceil(position * avgServiceTime);

            log.debug("Estimated wait time for position {} at Counter {}: {} minutes",
                position, counterName, estimatedMinutes);

            return estimatedMinutes;

        } catch (Exception e) {
            log.error("Error calculating wait time", e);
            // Return estimate based on default time
            return position * (int) DEFAULT_SERVICE_TIME;
        }
    }

    @Override
    public String getServiceTrend(CounterName counterName) {
        try {
            double currentAverage = getAverageServiceTime(counterName);
            double previousAverage = getPreviousAverageServiceTime(counterName);

            // Compare averages
            // If difference is less than 0.5 minutes, consider STABLE
            double difference = currentAverage - previousAverage;

            if (difference < -0.5) {
                return "FASTER";   // Getting faster ↓
            } else if (difference > 0.5) {
                return "SLOWER";   // Getting slower ↑
            } else {
                return "STABLE";   // No significant change →
            }

        } catch (Exception e) {
            log.error("Error calculating service trend", e);
            return "STABLE";
        }
    }

    @Override
    public double getPreviousAverageServiceTime(CounterName counterName) {
        try {
            // Get counter
            ServiceCounter counter = counterRepository
                .findByName(counterName)
                .orElse(null);

            if (counter == null) {
                return DEFAULT_SERVICE_TIME;
            }

            // Get completed tokens from yesterday
            List<Token> yesterdayTokens = tokenRepository
                .findLast10CompletedTokens(counter, LocalDate.now().minusDays(1));

            if (yesterdayTokens.isEmpty()) {
                return DEFAULT_SERVICE_TIME;
            }

            // Calculate yesterday's average
            double totalMinutes = 0;
            int validCount = 0;

            for (Token token : yesterdayTokens) {
                if (token.getServedAt() != null && token.getCompletedAt() != null) {
                    Duration duration = Duration.between(
                        token.getServedAt(),
                        token.getCompletedAt()
                    );
                    totalMinutes += duration.toMinutes();
                    validCount++;
                }
            }

            if (validCount == 0) {
                return DEFAULT_SERVICE_TIME;
            }

            return totalMinutes / validCount;

        } catch (Exception e) {
            log.error("Error calculating previous average", e);
            return DEFAULT_SERVICE_TIME;
        }
    }
}