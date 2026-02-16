package com.queue.management.repository;

import com.queue.management.entity.ServiceCounter;
import com.queue.management.entity.Token;
import com.queue.management.enums.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    
    // Find token by token code (e.g., "A-045")
    Optional<Token> findByTokenCode(String tokenCode);
    
    // Find student's token for today
    Optional<Token> findByStudent_RollNumberAndServiceDate(String rollNumber, LocalDate serviceDate);
    
    // Count tokens for a counter on a specific date
    long countByCounterAndServiceDate(ServiceCounter counter, LocalDate serviceDate);
    
    // Find all tokens by counter and status, ordered by token number
    List<Token> findByCounterAndStatusOrderByTokenNumberAsc(ServiceCounter counter, TokenStatus status);
    
    // Find currently serving token for a counter
    Optional<Token> findByCounterAndStatus(ServiceCounter counter, TokenStatus status);
    
    // Find all tokens by counter and date
    List<Token> findByCounterAndServiceDate(ServiceCounter counter, LocalDate serviceDate);
    
    // Find last 10 completed tokens for average time calculation
    @Query("SELECT t FROM Token t WHERE t.counter = :counter " +
           "AND t.status = 'COMPLETED' " +
           "AND t.serviceDate = :date " +
           "ORDER BY t.completedAt DESC")
    List<Token> findLast10CompletedTokens(@Param("counter") ServiceCounter counter, 
                                          @Param("date") LocalDate date);
}   