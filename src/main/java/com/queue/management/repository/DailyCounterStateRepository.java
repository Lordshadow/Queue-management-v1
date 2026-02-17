package com.queue.management.repository;

import com.queue.management.entity.DailyCounterState;
import com.queue.management.entity.ServiceCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyCounterStateRepository extends JpaRepository<DailyCounterState, Long> {
    
    // Find state for a specific counter on a specific date
    Optional<DailyCounterState> findByCounterAndServiceDate(ServiceCounter counter, LocalDate serviceDate);
    
    // Check if state exists for counter and date
    boolean existsByCounterAndServiceDate(ServiceCounter counter, LocalDate serviceDate);
}