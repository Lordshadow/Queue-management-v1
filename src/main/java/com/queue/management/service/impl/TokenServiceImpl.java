package com.queue.management.service.impl;

import com.queue.management.dto.response.TokenHistoryResponse;
import com.queue.management.dto.response.TokenNotification;
import com.queue.management.dto.response.TokenResponse;
import com.queue.management.entity.DailyCounterState;
import com.queue.management.entity.QueueRotationState;
import com.queue.management.entity.ServiceCounter;
import com.queue.management.entity.Student;
import com.queue.management.entity.Token;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.CounterStatus;
import com.queue.management.enums.NotificationType;
import com.queue.management.enums.TokenStatus;
import com.queue.management.repository.DailyCounterStateRepository;
import com.queue.management.repository.QueueRotationStateRepository;
import com.queue.management.repository.ServiceCounterRepository;
import com.queue.management.repository.StudentRepository;
import com.queue.management.repository.TokenRepository;
import com.queue.management.service.NotificationService;
import com.queue.management.service.StatisticsService;
import com.queue.management.service.TokenService;
import com.queue.management.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final StudentRepository studentRepository;
    private final ServiceCounterRepository counterRepository;
    private final DailyCounterStateRepository dailyCounterStateRepository;
    private final QueueRotationStateRepository queueRotationStateRepository;
    private final ValidationService validationService;
    private final StatisticsService statisticsService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TokenResponse generateToken(String rollNumber) {

        if (!validationService.canStudentGenerateToken(rollNumber)) {
            throw new RuntimeException(
                "Cannot generate token! Either outside working hours " +
                "or you already have an active token today."
            );
        }

        CounterName selectedCounter = selectCounter();

        if (!validationService.isCounterAcceptingTokens(selectedCounter)) {
            CounterName otherCounter = selectedCounter == CounterName.A ? CounterName.B : CounterName.A;
            if (!validationService.isCounterAcceptingTokens(otherCounter)) {
                throw new RuntimeException("No counters available right now!");
            }
            selectedCounter = otherCounter;
        }

        ServiceCounter counter = counterRepository
            .findByName(selectedCounter)
            .orElseThrow(() -> new RuntimeException("Counter not found!"));

        Student student = studentRepository
            .findByRollNumber(rollNumber)
            .orElseThrow(() -> new RuntimeException("Student not found!"));

        int nextNumber = getNextTokenNumber(counter);
        String tokenCode = selectedCounter.name() + "-" + String.format("%03d", nextNumber);

        Token token = Token.builder()
            .counter(counter)
            .student(student)
            .tokenNumber(nextNumber)
            .tokenCode(tokenCode)
            .status(TokenStatus.WAITING)
            .serviceDate(LocalDate.now())
            .build();

        tokenRepository.save(token);
        updateRotationState(counter.getId());

        log.info("Token generated: {} for student: {}", tokenCode, rollNumber);

        notificationService.notifyQueueUpdate(
            tokenCode, selectedCounter, TokenStatus.WAITING,
            countWaiting(counter), currentlyServing(counter),
            "New token " + tokenCode + " added to queue"
        );

        return buildTokenResponse(token, rollNumber);
    }

    @Override
    public TokenResponse getMyToken(String rollNumber) {
        Token token = tokenRepository
            .findTopByStudent_RollNumberAndStatusInOrderByCreatedAtDesc(
                rollNumber,
                EnumSet.of(TokenStatus.WAITING, TokenStatus.SERVING, TokenStatus.RESCHEDULED)
            )
            .orElseThrow(() -> new RuntimeException("No active token found!"));

        return buildTokenResponse(token, rollNumber);
    }

    @Override
    @Transactional
    public void cancelToken(String rollNumber) {
        // Allow cancelling WAITING or SERVING tokens (per spec)
        Token token = tokenRepository
            .findByStudent_RollNumberAndStatusAndServiceDate(rollNumber, TokenStatus.WAITING, LocalDate.now())
            .orElseGet(() ->
                tokenRepository.findByStudent_RollNumberAndStatusAndServiceDate(
                    rollNumber, TokenStatus.SERVING, LocalDate.now()
                ).orElseThrow(() -> new RuntimeException(
                    "No cancellable token found! Only WAITING or SERVING tokens can be cancelled."
                ))
            );

        token.setStatus(TokenStatus.DROPPED);
        tokenRepository.save(token);

        log.info("Token cancelled: {} by student: {}", token.getTokenCode(), rollNumber);

        notificationService.notifyQueueUpdate(
            token.getTokenCode(),
            token.getCounter().getName(),
            TokenStatus.DROPPED,
            countWaiting(token.getCounter()),
            currentlyServing(token.getCounter()),
            "Token " + token.getTokenCode() + " was cancelled by student"
        );
    }

    @Override
    @Transactional
    public TokenResponse callNextToken(CounterName counterName) {

        ServiceCounter counter = counterRepository
            .findByName(counterName)
            .orElseThrow(() -> new RuntimeException("Counter not found!"));

        if (counter.getStatus() != CounterStatus.ACTIVE) {
            throw new RuntimeException("Counter " + counterName + " is not active!");
        }

        List<Token> waitingTokens = tokenRepository
            .findByCounterAndStatusAndServiceDateOrderByTokenNumberAsc(
                counter, TokenStatus.WAITING, LocalDate.now()
            );

        if (waitingTokens.isEmpty()) {
            throw new RuntimeException("No waiting tokens for Counter " + counterName);
        }

        Token nextToken = waitingTokens.get(0);
        nextToken.setStatus(TokenStatus.SERVING);
        nextToken.setServedAt(LocalDateTime.now());
        tokenRepository.save(nextToken);

        log.info("Calling next token: {}", nextToken.getTokenCode());

        List<Token> remainingWaiting = waitingTokens.subList(1, waitingTokens.size());

        notificationService.notifyQueueUpdate(
            nextToken.getTokenCode(), counterName, TokenStatus.SERVING,
            remainingWaiting.size(), nextToken.getTokenCode(),
            "Counter " + counterName + " is now serving " + nextToken.getTokenCode()
        );

        sendPositionalNotifications(nextToken, remainingWaiting, counterName);

        return buildTokenResponse(nextToken, nextToken.getStudent().getRollNumber());
    }

    @Override
    @Transactional
    public TokenResponse completeToken(CounterName counterName) {

        ServiceCounter counter = counterRepository
            .findByName(counterName)
            .orElseThrow(() -> new RuntimeException("Counter not found!"));

        Token servingToken = tokenRepository
            .findTopByCounterAndStatusAndServiceDateOrderByServedAtDesc(
                counter, TokenStatus.SERVING, LocalDate.now())
            .orElseThrow(() -> new RuntimeException("No token currently being served!"));

        servingToken.setStatus(TokenStatus.COMPLETED);
        servingToken.setCompletedAt(LocalDateTime.now());
        tokenRepository.save(servingToken);

        log.info("Token completed: {}", servingToken.getTokenCode());

        // Notify waiting students before auto-calling
        sendCompletionAlerts(counter, counterName);

        notificationService.notifyQueueUpdate(
            servingToken.getTokenCode(), counterName, TokenStatus.COMPLETED,
            countWaiting(counter), null,
            "Token " + servingToken.getTokenCode() + " completed at Counter " + counterName
        );

        // Auto-call next token
        autoCallNext(counter, counterName);

        return buildTokenResponse(servingToken, null);
    }

    @Override
    @Transactional
    public TokenResponse dropToken(CounterName counterName) {

        ServiceCounter counter = counterRepository
            .findByName(counterName)
            .orElseThrow(() -> new RuntimeException("Counter not found!"));

        Token servingToken = tokenRepository
            .findTopByCounterAndStatusAndServiceDateOrderByServedAtDesc(
                counter, TokenStatus.SERVING, LocalDate.now())
            .orElseThrow(() -> new RuntimeException("No token currently being served!"));

        servingToken.setStatus(TokenStatus.DROPPED);
        tokenRepository.save(servingToken);

        log.info("Token dropped: {}", servingToken.getTokenCode());

        sendCompletionAlerts(counter, counterName);

        notificationService.notifyQueueUpdate(
            servingToken.getTokenCode(), counterName, TokenStatus.DROPPED,
            countWaiting(counter), null,
            "Token " + servingToken.getTokenCode() + " dropped at Counter " + counterName
        );

        autoCallNext(counter, counterName);

        return buildTokenResponse(servingToken, null);
    }

    @Override
    public int getQueuePosition(String rollNumber) {
        Token myToken = tokenRepository
            .findByStudent_RollNumberAndStatusAndServiceDate(
                rollNumber, TokenStatus.WAITING, LocalDate.now()
            )
            .orElse(null);

        if (myToken == null) return 0;

        List<Token> waitingTokens = tokenRepository
            .findByCounterAndStatusAndServiceDateOrderByTokenNumberAsc(
                myToken.getCounter(), TokenStatus.WAITING, LocalDate.now()
            );

        int position = 0;
        for (Token token : waitingTokens) {
            if (token.getTokenNumber() < myToken.getTokenNumber()) position++;
        }
        return position;
    }

    @Override
    public List<TokenHistoryResponse> getTokenHistory(String rollNumber) {
        List<Token> tokens = tokenRepository
            .findByStudent_RollNumberOrderByServiceDateDescCreatedAtDesc(rollNumber);

        List<TokenHistoryResponse> history = new ArrayList<>();
        for (Token token : tokens) {
            Long waitMinutes = null;
            Long serviceMinutes = null;

            if (token.getServedAt() != null) {
                waitMinutes = Duration.between(token.getCreatedAt(), token.getServedAt()).toMinutes();
            }
            if (token.getServedAt() != null && token.getCompletedAt() != null) {
                serviceMinutes = Duration.between(token.getServedAt(), token.getCompletedAt()).toMinutes();
            }

            history.add(TokenHistoryResponse.builder()
                .id(token.getId())
                .tokenCode(token.getTokenCode())
                .counterName(token.getCounter().getName())
                .status(token.getStatus())
                .serviceDate(token.getServiceDate())
                .createdAt(token.getCreatedAt())
                .servedAt(token.getServedAt())
                .completedAt(token.getCompletedAt())
                .isRescheduled(token.getIsRescheduled())
                .waitTimeMinutes(waitMinutes)
                .serviceTimeMinutes(serviceMinutes)
                .build());
        }
        return history;
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────────────

    private void autoCallNext(ServiceCounter counter, CounterName counterName) {
        List<Token> waiting = tokenRepository
            .findByCounterAndStatusAndServiceDateOrderByTokenNumberAsc(
                counter, TokenStatus.WAITING, LocalDate.now()
            );

        if (waiting.isEmpty()) {
            log.info("No more waiting tokens for Counter {}", counterName);
            return;
        }

        Token nextToken = waiting.get(0);
        nextToken.setStatus(TokenStatus.SERVING);
        nextToken.setServedAt(LocalDateTime.now());
        tokenRepository.save(nextToken);

        log.info("Auto-called next token: {}", nextToken.getTokenCode());

        List<Token> remainingWaiting = waiting.subList(1, waiting.size());

        notificationService.notifyQueueUpdate(
            nextToken.getTokenCode(), counterName, TokenStatus.SERVING,
            remainingWaiting.size(), nextToken.getTokenCode(),
            "Counter " + counterName + " auto-called " + nextToken.getTokenCode()
        );

        sendPositionalNotifications(nextToken, remainingWaiting, counterName);
    }

    private void sendPositionalNotifications(Token calledToken, List<Token> remainingWaiting,
                                              CounterName counterName) {
        if (remainingWaiting.isEmpty()) return;

        // First in remaining queue → YOUR_TURN
        Token nextInLine = remainingWaiting.get(0);
        notificationService.notifyStudent(
            nextInLine.getStudent().getRollNumber(),
            TokenNotification.builder()
                .type(NotificationType.YOUR_TURN)
                .tokenCode(nextInLine.getTokenCode())
                .counterName(counterName)
                .status(TokenStatus.WAITING)
                .waitingCount(remainingWaiting.size())
                .currentlyServing(calledToken.getTokenCode())
                .yourPosition(0)
                .message("You're next! Please head to Counter " + counterName)
                .build()
        );

        // Positions 1-3 → POSITION_ALERT (entered next 4 zone)
        for (int i = 1; i < Math.min(4, remainingWaiting.size()); i++) {
            Token t = remainingWaiting.get(i);
            notificationService.notifyStudent(
                t.getStudent().getRollNumber(),
                TokenNotification.builder()
                    .type(NotificationType.POSITION_ALERT)
                    .tokenCode(t.getTokenCode())
                    .counterName(counterName)
                    .status(TokenStatus.WAITING)
                    .waitingCount(remainingWaiting.size())
                    .currentlyServing(calledToken.getTokenCode())
                    .yourPosition(i)
                    .message("Your turn is coming soon! You're within the next 4 in queue.")
                    .build()
            );
        }
    }

    private void sendCompletionAlerts(ServiceCounter counter, CounterName counterName) {
        List<Token> waitingNow = tokenRepository
            .findByCounterAndStatusAndServiceDateOrderByTokenNumberAsc(
                counter, TokenStatus.WAITING, LocalDate.now()
            );

        for (int i = 0; i < Math.min(5, waitingNow.size()); i++) {
            Token t = waitingNow.get(i);
            notificationService.notifyStudent(
                t.getStudent().getRollNumber(),
                TokenNotification.builder()
                    .type(NotificationType.TOKEN_COMPLETED_AHEAD)
                    .tokenCode(t.getTokenCode())
                    .counterName(counterName)
                    .status(TokenStatus.WAITING)
                    .waitingCount(waitingNow.size())
                    .currentlyServing(null)
                    .yourPosition(i)
                    .message("A token just completed. You're getting closer! Position: " + (i + 1))
                    .build()
            );
        }
    }

    private CounterName selectCounter() {
        LocalDate today = LocalDate.now();
        Optional<QueueRotationState> rotationOpt =
            queueRotationStateRepository.findByServiceDate(today);

        if (rotationOpt.isEmpty()) return CounterName.A;

        Long lastUsedCounterId = rotationOpt.get().getLastUsedCounterId();
        ServiceCounter counterA = counterRepository.findByName(CounterName.A).orElseThrow();
        return lastUsedCounterId.equals(counterA.getId()) ? CounterName.B : CounterName.A;
    }

    @Transactional
    private void updateRotationState(Long counterId) {
        LocalDate today = LocalDate.now();
        Optional<QueueRotationState> rotationOpt =
            queueRotationStateRepository.findByServiceDate(today);

        if (rotationOpt.isEmpty()) {
            queueRotationStateRepository.save(QueueRotationState.builder()
                .serviceDate(today).lastUsedCounterId(counterId).build());
        } else {
            QueueRotationState state = rotationOpt.get();
            state.setLastUsedCounterId(counterId);
            queueRotationStateRepository.save(state);
        }
    }

    @Transactional
    private int getNextTokenNumber(ServiceCounter counter) {
        LocalDate today = LocalDate.now();
        DailyCounterState state = dailyCounterStateRepository
            .findByCounterAndServiceDate(counter, today)
            .orElseGet(() -> dailyCounterStateRepository.save(
                DailyCounterState.builder().counter(counter).serviceDate(today).lastTokenNumber(0).build()
            ));

        int nextNumber = state.getLastTokenNumber() + 1;
        state.setLastTokenNumber(nextNumber);
        dailyCounterStateRepository.save(state);
        return nextNumber;
    }

    private int countWaiting(ServiceCounter counter) {
        return tokenRepository.findByCounterAndStatusAndServiceDateOrderByTokenNumberAsc(
            counter, TokenStatus.WAITING, LocalDate.now()
        ).size();
    }

    private String currentlyServing(ServiceCounter counter) {
        return tokenRepository.findTopByCounterAndStatusAndServiceDateOrderByServedAtDesc(
            counter, TokenStatus.SERVING, LocalDate.now()
        ).map(Token::getTokenCode).orElse(null);
    }

    private TokenResponse buildTokenResponse(Token token, String rollNumber) {
        int position = 0;
        int estimatedWaitTime = 0;

        if (token.getStatus() == TokenStatus.WAITING && rollNumber != null) {
            position = getQueuePosition(rollNumber);
            estimatedWaitTime = statisticsService.getEstimatedWaitTime(
                token.getCounter().getName(), position
            );
        }

        return TokenResponse.builder()
            .id(token.getId())
            .tokenCode(token.getTokenCode())
            .tokenNumber(token.getTokenNumber())
            .counterName(token.getCounter().getName())
            .status(token.getStatus())
            .serviceDate(token.getServiceDate())
            .position(position)
            .estimatedWaitTime(estimatedWaitTime)
            .createdAt(token.getCreatedAt())
            .servedAt(token.getServedAt())
            .completedAt(token.getCompletedAt())
            .isRescheduled(token.getIsRescheduled())
            .build();
    }
}
