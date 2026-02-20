package com.queue.management.repository;

import com.queue.management.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Find a valid (not used) token for reset
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    // Delete all previous reset tokens for an email before issuing a new one
    void deleteByEmail(String email);

    // Check if an unexpired token already exists for this email
    boolean existsByEmailAndUsedFalse(String email);
}
