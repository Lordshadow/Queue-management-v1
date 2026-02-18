package com.queue.management.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.queue.management.entity.CounterStaff;
import com.queue.management.entity.Student;
import com.queue.management.enums.CounterName;
import com.queue.management.enums.UserType;
import com.queue.management.repository.CounterStaffRepository;
import com.queue.management.repository.StudentRepository;
import com.queue.management.security.JwtTokenProvider;
import com.queue.management.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final StudentRepository studentRepository;
    private final CounterStaffRepository counterStaffRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String registerStudent(String rollNumber,
                                  String name,
                                  String email,
                                  String password) {
        // Check if roll number already exists
        if (studentRepository.existsByRollNumber(rollNumber)) {
            throw new RuntimeException(
                "Roll number already registered: " + rollNumber
            );
        }

        // Check if email already exists
        if (studentRepository.existsByEmail(email)) {
            throw new RuntimeException(
                "Email already registered: " + email
            );
        }

        // Create new student
        Student student = Student.builder()
                .rollNumber(rollNumber)
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password)) // Hash password!
                .build();

        // Save to database
        studentRepository.save(student);

        log.info("Student registered successfully: {}", rollNumber);
        return "Student registered successfully!";
    }

    @Override
    public String login(String identifier,
                       String password,
                       UserType userType,
                       CounterName selectedCounter) {

        if (userType == UserType.STUDENT) {
            return loginStudent(identifier, password);
        } else {
            return loginStaff(identifier, password, selectedCounter);
        }
    }

    private String loginStudent(String rollNumber, String password) {
        // Find student
        Student student = studentRepository
                .findByRollNumber(rollNumber)
                .orElseThrow(() -> new RuntimeException(
                    "Student not found with roll number: " + rollNumber
                ));

        // Verify password
        if (!passwordEncoder.matches(password, student.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateTokenForStudent(
                student.getRollNumber(),
                student.getName()
        );

        log.info("Student logged in: {}", rollNumber);
        return token;
    }

    private String loginStaff(String staffId,
                              String password,
                              CounterName selectedCounter) {
        // Validate counter selection
        if (selectedCounter == null) {
            throw new RuntimeException(
                "Counter selection is required for staff login!"
            );
        }

        // Find staff
        CounterStaff staff = counterStaffRepository
                .findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException(
                    "Staff not found with ID: " + staffId
                ));
                // DEBUG: Print password details
    log.info("=== DEBUG LOGIN ===");
    log.info("Input password: {}", password);
    log.info("Stored hash: {}", staff.getPassword());
    log.info("Match result: {}", passwordEncoder.matches(password, staff.getPassword()));
    log.info("===================");


        // Verify password
        if (!passwordEncoder.matches(password, staff.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        // Generate JWT token with counter info
        String token = jwtTokenProvider.generateTokenForStaff(
                staff.getStaffId(),
                staff.getName(),
                selectedCounter
        );

        log.info("Staff logged in: {} on Counter {}", staffId, selectedCounter);
        return token;
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        // Check if email exists
        boolean studentExists = studentRepository.existsByEmail(email);
        boolean staffExists = counterStaffRepository.existsByEmail(email);

        if (!studentExists && !staffExists) {
            throw new RuntimeException("Email not found: " + email);
        }

        // TODO: Implement email sending later
        log.info("Password reset email requested for: {}", email);
    }

    @Override
    public void resetPassword(String resetToken, String newPassword) {
        // TODO: Implement password reset with token
        log.info("Password reset requested");
    }

    @Override
    public void changePassword(String identifier,
                              String currentPassword,
                              String newPassword,
                              UserType userType) {
        if (userType == UserType.STUDENT) {
            // Find student
            Student student = studentRepository
                    .findByRollNumber(identifier)
                    .orElseThrow(() -> new RuntimeException("Student not found!"));

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, student.getPassword())) {
                throw new RuntimeException("Current password is incorrect!");
            }

            // Update password
            student.setPassword(passwordEncoder.encode(newPassword));
            studentRepository.save(student);

            log.info("Password changed for student: {}", identifier);

        } else {
            // Find staff
            CounterStaff staff = counterStaffRepository
                    .findByStaffId(identifier)
                    .orElseThrow(() -> new RuntimeException("Staff not found!"));

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, staff.getPassword())) {
                throw new RuntimeException("Current password is incorrect!");
            }

            // Update password
            staff.setPassword(passwordEncoder.encode(newPassword));
            counterStaffRepository.save(staff);

            log.info("Password changed for staff: {}", identifier);
        }
    }

    @Override
    public boolean emailExists(String email) {
        return studentRepository.existsByEmail(email)
                || counterStaffRepository.existsByEmail(email);
    }
}