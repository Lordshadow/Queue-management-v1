package com.queue.management.service.impl;

import com.queue.management.dto.response.TokenResponse;
import com.queue.management.entity.DailyCounterState;
import com.queue.management.entity.QueueRotationState;
import com.queue.management.entity.ServiceCounter;
import com.queue.management.entity.Student;
import com.queue.management.entity.Token;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.CounterStatus;
import com.queue.management.enums.TokenStatus;
import com.queue.management.repository.DailyCounterStateRepository;
import com.queue.management.repository.QueueRotationStateRepository;
import com.queue.management.repository.ServiceCounterRepository;
import com.queue.management.repository.StudentRepository;
import com.queue.management.repository.TokenRepository;
import com.queue.management.service.StatisticsService;
import com.queue.management.service.TokenService;
import com.queue.management.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Override
    @Transactional
    public TokenResponse generateToken(String rollNumber) {

        // Step 1: Check if student can generate token
        if (!validationService.canStudentGenerateToken(rollNumber)) {
            throw new RuntimeException(
                "Cannot generate token! Either outside working hours " +
                "or you already have an active token today."
            );
        }

        // Step 2: Select which counter to assign (round-robin)
        CounterName selectedCounter = selectCounter();

        // Step 3: Check if selected counter is accepting tokens
        if (!validationService.isCounterAcceptingTokens(selectedCounter)) {
            // Try the other counter
            CounterName otherCounter = selectedCounter == CounterName.A
                ? CounterName.B : CounterName.A;

            if (!validationService.isCounterAcceptingTokens(otherCounter)) {
                throw new RuntimeException(
                    "No counters available right now!"
                );
            }
            selectedCounter = otherCounter;
        }

        // Step 4: Get counter entity
        ServiceCounter counter = counterRepository
            .findByName(selectedCounter)
            .orElseThrow(() -> new RuntimeException("Counter not found!"));

        // Step 5: Get student entity
        Student student = studentRepository
            .findByRollNumber(rollNumber)
            .orElseThrow(() -> new RuntimeException("Student not found!"));

        // Step 6: Get next token number
        int nextNumber = getNextTokenNumber(counter);

        // Step 7: Generate token code (e.g., "A-046")
        String tokenCode = selectedCounter.name() + "-" +
            String.format("%03d", nextNumber);

        // Step 8: Create token
        Token token = Token.builder()
            .counter(counter)
            .student(student)
            .tokenNumber(nextNumber)
            .tokenCode(tokenCode)
            .status(TokenStatus.WAITING)
            .serviceDate(LocalDate.now())
            .build();

        // Step 9: Save token
        tokenRepository.save(token);

        // Step 10: Update rotation state
        updateRotationState(counter.getId());

        log.info("Token generated: {} for student: {}", tokenCode, rollNumber);

        // Step 11: Return response
        return buildTokenResponse(token, rollNumber);
    }

    @Override
    public TokenResponse getMyToken(String rollNumber) {

        Token token = tokenRepository
            .findByStudent_RollNumberAndServiceDate(rollNumber, LocalDate.now())
            .orElseThrow(() -> new RuntimeException(
                "No token found for today!"
            ));

        return buildTokenResponse(token, rollNumber);
    }

    @Override
    @Transactional
    public void cancelToken(String rollNumber) {

        Token token = tokenRepository
            .findByStudent_RollNumberAndServiceDate(rollNumber, LocalDate.now())
            .orElseThrow(() -> new RuntimeException(
                "No active token found!"
            ));

        // Can only cancel WAITING tokens
        if (token.getStatus() != TokenStatus.WAITING) {
            throw new RuntimeException(
                "Cannot cancel token with status: " + token.getStatus()
            );
        }

        // Mark as DROPPED
        token.setStatus(TokenStatus.DROPPED);
        tokenRepository.save(token);

        log.info("Token cancelled: {} by student: {}",
            token.getTokenCode(), rollNumber);
    }

    @Override
    @Transactional
    public TokenResponse callNextToken(CounterName counterName) {

        // Get counter
        ServiceCounter counter = counterRepository
            .findByName(counterName)
            .orElseThrow(() -> new RuntimeException("Counter not found!"));

        // Check if counter is active
        if (counter.getStatus() != CounterStatus.ACTIVE) {
            throw new RuntimeException(
                "Counter " + counterName + " is not active!"
            );
        }

        // Find next WAITING token
        List<Token> waitingTokens = tokenRepository
            .findByCounterAndStatusOrderByTokenNumberAsc(
                counter, TokenStatus.WAITING
            );

        if (waitingTokens.isEmpty()) {
            throw new RuntimeException(
                "No waiting tokens for Counter " + counterName
            );
        }

        // Get first waiting token
        Token nextToken = waitingTokens.get(0);

        // Update status to SERVING
        nextToken.setStatus(TokenStatus.SERVING);
        nextToken.setServedAt(LocalDateTime.now());
        tokenRepository.save(nextToken);

        log.info("Calling next token: {}", nextToken.getTokenCode());

        return buildTokenResponse(nextToken, null);
    }

    @Override
    @Transactional
    public TokenResponse completeToken(CounterName counterName) {

        // Get currently serving token
        ServiceCounter counter = counterRepository
            .findByName(counterName)
            .orElseThrow(() -> new RuntimeException("Counter not found!"));

        Token servingToken = tokenRepository
            .findByCounterAndStatus(counter, TokenStatus.SERVING)
            .orElseThrow(() -> new RuntimeException(
                "No token currently being served!"
            ));

        // Mark as COMPLETED
        servingToken.setStatus(TokenStatus.COMPLETED);
        servingToken.setCompletedAt(LocalDateTime.now());
        tokenRepository.save(servingToken);

        log.info("Token completed: {}", servingToken.getTokenCode());

        return buildTokenResponse(servingToken, null);
    }

    @Override
    @Transactional
    public TokenResponse dropToken(CounterName counterName) {

        // Get currently serving token
        ServiceCounter counter = counterRepository
            .findByName(counterName)
            .orElseThrow(() -> new RuntimeException("Counter not found!"));

        Token servingToken = tokenRepository
            .findByCounterAndStatus(counter, TokenStatus.SERVING)
            .orElseThrow(() -> new RuntimeException(
                "No token currently being served!"
            ));

        // Mark as DROPPED
        servingToken.setStatus(TokenStatus.DROPPED);
        servingToken.setCompletedAt(LocalDateTime.now());
        tokenRepository.save(servingToken);

        log.info("Token dropped: {}", servingToken.getTokenCode());

        return buildTokenResponse(servingToken, null);
    }

    @Override
    public int getQueuePosition(String rollNumber) {

        // Find student's token
        Token myToken = tokenRepository
            .findByStudent_RollNumberAndServiceDate(rollNumber, LocalDate.now())
            .orElse(null);

        if (myToken == null || myToken.getStatus() != TokenStatus.WAITING) {
            return 0;
        }

        // Count tokens ahead (lower number = ahead in queue)
        List<Token> waitingTokens = tokenRepository
            .findByCounterAndStatusOrderByTokenNumberAsc(
                myToken.getCounter(), TokenStatus.WAITING
            );

        int position = 0;
        for (Token token : waitingTokens) {
            if (token.getTokenNumber() < myToken.getTokenNumber()) {
                position++;
            }
        }

        return position;
    }

    // ─── PRIVATE HELPER METHODS ───────────────────────────────────────────

    // Select counter using round-robin alternation
    private CounterName selectCounter() {

        LocalDate today = LocalDate.now();

        // Check rotation state
        Optional<QueueRotationState> rotationOpt =
            queueRotationStateRepository.findByServiceDate(today);

        if (rotationOpt.isEmpty()) {
            // First token of the day → Start with Counter A
            return CounterName.A;
        }

        // Get last used counter
        Long lastUsedCounterId = rotationOpt.get().getLastUsedCounterId();

        // Find counter A's ID
        ServiceCounter counterA = counterRepository
            .findByName(CounterName.A)
            .orElseThrow();

        // Alternate: if last was A → use B, if last was B → use A
        if (lastUsedCounterId.equals(counterA.getId())) {
            return CounterName.B;
        } else {
            return CounterName.A;
        }
    }

    // Update rotation state after token generation
    @Transactional
    private void updateRotationState(Long counterId) {

        LocalDate today = LocalDate.now();

        Optional<QueueRotationState> rotationOpt =
            queueRotationStateRepository.findByServiceDate(today);

        if (rotationOpt.isEmpty()) {
            // Create new rotation state
            QueueRotationState newState = QueueRotationState.builder()
                .serviceDate(today)
                .lastUsedCounterId(counterId)
                .build();
            queueRotationStateRepository.save(newState);
        } else {
            // Update existing rotation state
            QueueRotationState state = rotationOpt.get();
            state.setLastUsedCounterId(counterId);
            queueRotationStateRepository.save(state);
        }
    }

    // Get next token number for a counter (thread-safe)
    @Transactional
    private int getNextTokenNumber(ServiceCounter counter) {

        LocalDate today = LocalDate.now();

        // Find or create daily counter state
        DailyCounterState state = dailyCounterStateRepository
            .findByCounterAndServiceDate(counter, today)
            .orElseGet(() -> {
                // Create new state for today
                DailyCounterState newState = DailyCounterState.builder()
                    .counter(counter)
                    .serviceDate(today)
                    .lastTokenNumber(0)
                    .build();
                return dailyCounterStateRepository.save(newState);
            });

        // Increment token number
        int nextNumber = state.getLastTokenNumber() + 1;
        state.setLastTokenNumber(nextNumber);
        dailyCounterStateRepository.save(state);

        return nextNumber;
    }

    // Build TokenResponse from Token entity
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