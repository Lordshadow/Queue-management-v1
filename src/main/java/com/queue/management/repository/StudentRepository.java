package com.queue.management.repository;

import com.queue.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // Find student by roll number
    Optional<Student> findByRollNumber(String rollNumber);
    
    // Find student by email
    Optional<Student> findByEmail(String email);
    
    // Check if roll number exists
    boolean existsByRollNumber(String rollNumber);
    
    // Check if email exists
    boolean existsByEmail(String email);
}