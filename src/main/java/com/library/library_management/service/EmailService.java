package com.library.library_management.service;

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
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Send email verification link
     */
    @Async
    public void sendVerificationEmail(String to, String name, String token) {
        String subject = "Verify your email - " + appName;
        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        
        String htmlContent = buildVerificationEmailTemplate(name, verificationLink);
        
        sendHtmlEmail(to, subject, htmlContent);
        log.info("Verification email sent to: {}", to);
    }

    /**
     * Send password reset link
     */
    @Async
    public void sendPasswordResetEmail(String to, String name, String token) {
        String subject = "Reset your password - " + appName;
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        
        String htmlContent = buildPasswordResetEmailTemplate(name, resetLink);
        
        sendHtmlEmail(to, subject, htmlContent);
        log.info("Password reset email sent to: {}", to);
    }

    /**
     * Send welcome email after verification
     */
    @Async
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Welcome to " + appName + "!";
        String htmlContent = buildWelcomeEmailTemplate(name);
        
        sendHtmlEmail(to, subject, htmlContent);
        log.info("Welcome email sent to: {}", to);
    }

    /**
     * Send password changed confirmation
     */
    @Async
    public void sendPasswordChangedEmail(String to, String name) {
        String subject = "Password changed - " + appName;
        String htmlContent = buildPasswordChangedEmailTemplate(name);
        
        sendHtmlEmail(to, subject, htmlContent);
        log.info("Password changed email sent to: {}", to);
    }

    /**
     * Core method to send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // ========== Email Templates ==========

    private String buildVerificationEmailTemplate(String name, String verificationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background-color: #4F46E5; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .button:hover { background-color: #4338CA; }
                    .footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 12px; }
                    .link { color: #4F46E5; word-break: break-all; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s!</h2>
                        <p>Thank you for registering. Please verify your email address by clicking the button below:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Verify Email</a>
                        </p>
                        <p>Or copy and paste this link into your browser:</p>
                        <p class="link">%s</p>
                        <p>This link will expire in 24 hours.</p>
                        <p>If you didn't create an account, you can safely ignore this email.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(appName, name, verificationLink, verificationLink, appName);
    }

    private String buildPasswordResetEmailTemplate(String name, String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #DC2626; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background-color: #DC2626; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 12px; }
                    .link { color: #DC2626; word-break: break-all; }
                    .warning { background-color: #FEF3C7; border-left: 4px solid #F59E0B; padding: 10px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>We received a request to reset your password. Click the button below to create a new password:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </p>
                        <p>Or copy and paste this link into your browser:</p>
                        <p class="link">%s</p>
                        <div class="warning">
                            <strong>⚠️ Important:</strong> This link will expire in 1 hour for security reasons.
                        </div>
                        <p>If you didn't request a password reset, please ignore this email or contact support if you have concerns.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, resetLink, resetLink, appName);
    }

    private String buildWelcomeEmailTemplate(String name) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #059669; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background-color: #059669; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 12px; }
                    .feature { margin: 10px 0; padding-left: 20px; }
                    .feature:before { content: "✓ "; color: #059669; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome! 🎉</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s!</h2>
                        <p>Your email has been verified and your account is now active. Welcome to %s!</p>
                        <p>Here's what you can do:</p>
                        <div class="feature">Add and organize your book collection</div>
                        <div class="feature">Track your reading progress</div>
                        <div class="feature">Set reading goals and earn achievements</div>
                        <div class="feature">Get personalized book recommendations</div>
                        <div class="feature">Ask questions about your library using AI</div>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Start Exploring</a>
                        </p>
                        <p>Happy reading!</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, appName, frontendUrl, appName);
    }

    private String buildPasswordChangedEmailTemplate(String name) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #059669; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 12px; }
                    .warning { background-color: #FEE2E2; border-left: 4px solid #DC2626; padding: 10px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Changed</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Your password has been successfully changed.</p>
                        <p>If you made this change, you can safely ignore this email.</p>
                        <div class="warning">
                            <strong>⚠️ Didn't change your password?</strong><br>
                            If you didn't make this change, please contact our support team immediately and secure your account.
                        </div>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, appName);
    }
}