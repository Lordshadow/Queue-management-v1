package com.queue.management.repository;

import com.queue.management.entity.QueueRotationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface QueueRotationStateRepository extends JpaRepository<QueueRotationState, Long> {
    
    // Find rotation state for a specific date
    Optional<QueueRotationState> findByServiceDate(LocalDate serviceDate);
    
    // Check if rotation state exists for a date
    boolean existsByServiceDate(LocalDate serviceDate);
}