package com.queue.management.repository;

import com.queue.management.entity.CounterBreakLog;
import com.queue.management.entity.ServiceCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CounterBreakLogRepository extends JpaRepository<CounterBreakLog, Long> {
    
    // Find all break logs for a specific counter
    List<CounterBreakLog> findByCounterOrderByBreakStartDesc(ServiceCounter counter);
    
    // Find break logs for a counter between dates
    List<CounterBreakLog> findByCounterAndBreakStartBetween(
        ServiceCounter counter, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    // Find currently active break (break started but not ended)
    List<CounterBreakLog> findByCounterAndBreakEndIsNull(ServiceCounter counter);
}