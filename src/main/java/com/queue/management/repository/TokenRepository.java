package com.queue.management.repository;

import com.queue.management.entity.ServiceCounter;
import com.queue.management.entity.Token;
import com.queue.management.enums.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    // Find token by token code (e.g., "A-045")
    Optional<Token> findByTokenCode(String tokenCode);

    // Count tokens for a counter on a specific date
    long countByCounterAndServiceDate(ServiceCounter counter, LocalDate serviceDate);

    // Find all WAITING tokens for a counter on a specific date, ordered by token number
    List<Token> findByCounterAndStatusAndServiceDateOrderByTokenNumberAsc(
            ServiceCounter counter, TokenStatus status, LocalDate serviceDate);

    // Find the currently SERVING token for a counter on a specific date
    // Uses findTop to safely handle edge cases where stale SERVING rows exist
    Optional<Token> findTopByCounterAndStatusAndServiceDateOrderByServedAtDesc(
            ServiceCounter counter, TokenStatus status, LocalDate serviceDate);

    // Find all tokens by counter and date
    List<Token> findByCounterAndServiceDate(ServiceCounter counter, LocalDate serviceDate);

    // Find the most recent token for a student regardless of date (for getMyToken)
    // Returns WAITING, SERVING, or RESCHEDULED tokens so rescheduled tokens are visible too
    Optional<Token> findTopByStudent_RollNumberAndStatusInOrderByCreatedAtDesc(
            String rollNumber, Collection<TokenStatus> statuses);

    // Find student's token with specific status for a specific date (for cancel/position)
    Optional<Token> findByStudent_RollNumberAndStatusAndServiceDate(
            String rollNumber, TokenStatus status, LocalDate serviceDate);

    // Check if student has any active token today (for validation - avoids multi-row crash)
    boolean existsByStudent_RollNumberAndStatusInAndServiceDate(
            String rollNumber, Collection<TokenStatus> statuses, LocalDate serviceDate);

    // Find last 10 completed tokens for average time calculation
    @Query("SELECT t FROM Token t WHERE t.counter = :counter " +
           "AND t.status = 'COMPLETED' " +
           "AND t.serviceDate = :date " +
           "ORDER BY t.completedAt DESC " +
           "LIMIT 10")
    List<Token> findLast10CompletedTokens(@Param("counter") ServiceCounter counter,
                                          @Param("date") LocalDate date);
}
