package com.queue.management.service;

public interface EmailService {

    /**
     * Send a password reset email containing a reset link.
     *
     * @param toEmail   recipient email address
     * @param resetLink full URL the user should follow to reset their password
     */
    void sendPasswordResetEmail(String toEmail, String resetLink);
}
