package com.queue.management.service.impl;

import com.queue.management.dto.response.ProfileResponse;
import com.queue.management.entity.CounterStaff;
import com.queue.management.entity.PasswordResetToken;
import com.queue.management.entity.Student;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.UserType;
import com.queue.management.repository.CounterStaffRepository;
import com.queue.management.repository.PasswordResetTokenRepository;
import com.queue.management.repository.StudentRepository;
import com.queue.management.security.JwtTokenProvider;
import com.queue.management.service.AuthService;
import com.queue.management.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final StudentRepository studentRepository;
    private final CounterStaffRepository counterStaffRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.reset-token.expiry-hours}")
    private int resetTokenExpiryHours;

    // ─── REGISTER ──────────────────────────────────────────────────────────

    @Override
    public String registerStudent(String rollNumber, String name, String email, String password) {
        if (studentRepository.existsByRollNumber(rollNumber)) {
            throw new RuntimeException("Roll number already registered: " + rollNumber);
        }
        if (studentRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered: " + email);
        }

        Student student = Student.builder()
                .rollNumber(rollNumber)
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        studentRepository.save(student);
        log.info("Student registered successfully: {}", rollNumber);
        return "Student registered successfully!";
    }

    // ─── LOGIN ─────────────────────────────────────────────────────────────

    @Override
    public String login(String identifier, String password, UserType userType, CounterName selectedCounter) {
        if (userType == UserType.STUDENT) {
            return loginStudent(identifier, password);
        } else {
            return loginStaff(identifier, password, selectedCounter);
        }
    }

    private String loginStudent(String rollNumber, String password) {
        Student student = studentRepository
                .findByRollNumber(rollNumber)
                .orElseThrow(() -> new RuntimeException("Student not found with roll number: " + rollNumber));

        if (!passwordEncoder.matches(password, student.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        String token = jwtTokenProvider.generateTokenForStudent(student.getRollNumber(), student.getName());
        log.info("Student logged in: {}", rollNumber);
        return token;
    }

    private String loginStaff(String staffId, String password, CounterName selectedCounter) {
        if (selectedCounter == null) {
            throw new RuntimeException("Counter selection is required for staff login!");
        }

        CounterStaff staff = counterStaffRepository
                .findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));

        if (!passwordEncoder.matches(password, staff.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        String token = jwtTokenProvider.generateTokenForStaff(staff.getStaffId(), staff.getName(), selectedCounter);
        log.info("Staff logged in: {} on Counter {}", staffId, selectedCounter);
        return token;
    }

    // ─── FORGOT PASSWORD ───────────────────────────────────────────────────

    @Override
    @Transactional
    public void sendPasswordResetEmail(String email) {
        boolean studentExists = studentRepository.existsByEmail(email);
        boolean staffExists   = counterStaffRepository.existsByEmail(email);

        if (!studentExists && !staffExists) {
            throw new RuntimeException("No account found with email: " + email);
        }

        // Invalidate any previous reset tokens for this email
        passwordResetTokenRepository.deleteByEmail(email);

        // Generate a secure random UUID token
        String rawToken = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email)
                .token(rawToken)
                .expiresAt(LocalDateTime.now().plusHours(resetTokenExpiryHours))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Build the reset link pointing to the frontend reset-password page
        String resetLink = frontendUrl + "/reset-password?token=" + rawToken;

        // Send email asynchronously
        emailService.sendPasswordResetEmail(email, resetLink);

        log.info("Password reset token issued for: {}", email);
    }

    // ─── RESET PASSWORD ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(rawToken)
                .orElseThrow(() -> new RuntimeException("Invalid or already used reset token!"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Reset token has expired. Please request a new password reset.");
        }

        String email = resetToken.getEmail();

        // Update password for student, then staff (only one will match)
        studentRepository.findByEmail(email).ifPresent(student -> {
            student.setPassword(passwordEncoder.encode(newPassword));
            studentRepository.save(student);
            log.info("Password reset for student with email: {}", email);
        });

        counterStaffRepository.findByEmail(email).ifPresent(staff -> {
            staff.setPassword(passwordEncoder.encode(newPassword));
            counterStaffRepository.save(staff);
            log.info("Password reset for staff with email: {}", email);
        });

        // Mark token as used so it can't be replayed
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    // ─── CHANGE PASSWORD ───────────────────────────────────────────────────

    @Override
    public void changePassword(String identifier, String currentPassword, String newPassword, UserType userType) {
        if (userType == UserType.STUDENT) {
            Student student = studentRepository
                    .findByRollNumber(identifier)
                    .orElseThrow(() -> new RuntimeException("Student not found!"));

            if (!passwordEncoder.matches(currentPassword, student.getPassword())) {
                throw new RuntimeException("Current password is incorrect!");
            }

            student.setPassword(passwordEncoder.encode(newPassword));
            studentRepository.save(student);
            log.info("Password changed for student: {}", identifier);

        } else {
            CounterStaff staff = counterStaffRepository
                    .findByStaffId(identifier)
                    .orElseThrow(() -> new RuntimeException("Staff not found!"));

            if (!passwordEncoder.matches(currentPassword, staff.getPassword())) {
                throw new RuntimeException("Current password is incorrect!");
            }

            staff.setPassword(passwordEncoder.encode(newPassword));
            counterStaffRepository.save(staff);
            log.info("Password changed for staff: {}", identifier);
        }
    }

    // ─── MISC ──────────────────────────────────────────────────────────────

    @Override
    public boolean emailExists(String email) {
        return studentRepository.existsByEmail(email)
                || counterStaffRepository.existsByEmail(email);
    }

    @Override
    public ProfileResponse getStudentProfile(String rollNumber) {
        Student student = studentRepository
                .findByRollNumber(rollNumber)
                .orElseThrow(() -> new RuntimeException("Student not found!"));

        return ProfileResponse.builder()
                .rollNumber(student.getRollNumber())
                .name(student.getName())
                .email(student.getEmail())
                .registeredAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    @Override
    public void updateStudentProfile(String rollNumber, String name, String email) {
        Student student = studentRepository
                .findByRollNumber(rollNumber)
                .orElseThrow(() -> new RuntimeException("Student not found!"));

        if (!student.getEmail().equals(email) && studentRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use by another account!");
        }

        student.setName(name);
        student.setEmail(email);
        studentRepository.save(student);
        log.info("Profile updated for student: {}", rollNumber);
    }
}