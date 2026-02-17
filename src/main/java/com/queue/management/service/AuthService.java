package com.queue.management.service;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.UserType;

public interface AuthService {

    // Register a new student
    // Returns success message
    String registerStudent(String rollNumber, 
                          String name, 
                          String email, 
                          String password);

    // Login for both student and counter staff
    // Returns JWT token
    String login(String identifier, 
                String password, 
                UserType userType,
                CounterName selectedCounter);

    // Send password reset email
    void sendPasswordResetEmail(String email);

    // Reset password using token
    void resetPassword(String resetToken, String newPassword);

    // Change password for logged in user
    void changePassword(String identifier, 
                       String currentPassword, 
                       String newPassword,
                       UserType userType);

    // Validate if email exists for password reset
    boolean emailExists(String email);
}