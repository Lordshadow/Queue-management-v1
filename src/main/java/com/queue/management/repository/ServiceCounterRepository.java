package com.queue.management.repository;

import com.queue.management.entity.ServiceCounter;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.CounterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCounterRepository extends JpaRepository<ServiceCounter, Long> {
    
    // Find counter by name (A or B)
    Optional<ServiceCounter> findByName(CounterName name);
    
    // Find all available counters (status = ACTIVE)
    List<ServiceCounter> findByStatus(CounterStatus status);
    
    // Find all ACTIVE counters
    List<ServiceCounter> findByStatusOrderByNameAsc(CounterStatus status);
}