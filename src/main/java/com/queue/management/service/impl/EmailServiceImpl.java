package com.queue.management.service.impl;

import com.queue.management.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, "Queue Management System");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");
            helper.setText(buildEmailBody(resetLink), true); // true = HTML

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }

    private String buildEmailBody(String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0; padding:0; background-color:#f4f6f9; font-family:Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9; padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="600" cellpadding="0" cellspacing="0"
                           style="background-color:#ffffff; border-radius:8px;
                                  box-shadow:0 2px 8px rgba(0,0,0,0.08); overflow:hidden;">

                      <!-- Header -->
                      <tr>
                        <td style="background-color:#1a73e8; padding:32px 40px; text-align:center;">
                          <h1 style="margin:0; color:#ffffff; font-size:24px; font-weight:700;">
                            Queue Management System
                          </h1>
                          <p style="margin:8px 0 0; color:#d0e4ff; font-size:14px;">
                            Password Reset Request
                          </p>
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding:40px;">
                          <p style="margin:0 0 16px; color:#333333; font-size:16px;">
                            Hi there,
                          </p>
                          <p style="margin:0 0 16px; color:#555555; font-size:15px; line-height:1.6;">
                            We received a request to reset the password for your account.
                            Click the button below to set a new password. This link expires in
                            <strong>2 hours</strong>.
                          </p>

                          <!-- CTA Button -->
                          <table cellpadding="0" cellspacing="0" style="margin:32px 0;">
                            <tr>
                              <td style="background-color:#1a73e8; border-radius:6px;">
                                <a href="%s"
                                   style="display:inline-block; padding:14px 32px;
                                          color:#ffffff; font-size:15px; font-weight:600;
                                          text-decoration:none; border-radius:6px;">
                                  Reset My Password
                                </a>
                              </td>
                            </tr>
                          </table>

                          <!-- Fallback link -->
                          <p style="margin:0 0 8px; color:#555555; font-size:13px;">
                            If the button doesn't work, copy and paste this link into your browser:
                          </p>
                          <p style="margin:0 0 24px; word-break:break-all;">
                            <a href="%s" style="color:#1a73e8; font-size:13px;">%s</a>
                          </p>

                          <hr style="border:none; border-top:1px solid #e8ecf0; margin:24px 0;">

                          <p style="margin:0; color:#888888; font-size:13px; line-height:1.6;">
                            If you did not request a password reset, you can safely ignore this email.
                            Your password will not change.
                          </p>
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="background-color:#f4f6f9; padding:20px 40px; text-align:center;">
                          <p style="margin:0; color:#aaaaaa; font-size:12px;">
                            &copy; 2025 Queue Management System. All rights reserved.
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(resetLink, resetLink, resetLink);
    }
}
