package com.queue.management.repository;

import com.queue.management.entity.CounterStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CounterStaffRepository extends JpaRepository<CounterStaff, Long> {
    
    // Find staff by staff ID
    Optional<CounterStaff> findByStaffId(String staffId);
    
    // Find staff by email
    Optional<CounterStaff> findByEmail(String email);
    
    // Check if staff ID exists
    boolean existsByStaffId(String staffId);
    
    // Check if email exists
    boolean existsByEmail(String email);
}