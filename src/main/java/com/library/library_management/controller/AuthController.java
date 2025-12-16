package com.library.library_management.controller;

import com.library.library_management.dto.request.ForgotPasswordRequest;
import com.library.library_management.dto.request.LoginRequest;
import com.library.library_management.dto.request.RegisterRequest;
import com.library.library_management.dto.request.ResendVerificationRequest;
import com.library.library_management.dto.request.ResetPasswordRequest;
import com.library.library_management.dto.response.AuthResponse;
import com.library.library_management.dto.response.MessageResponse;
import com.library.library_management.security.CustomUserDetails;
import com.library.library_management.service.AuthService;
import com.library.library_management.service.TokenBlacklistService;
import com.library.library_management.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - Registering user: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - Login attempt: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current authenticated user info
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/auth/me - Getting current user info");
        AuthResponse.UserInfo userInfo = authService.getCurrentUser(userDetails.getId());
        return ResponseEntity.ok(userInfo);
    }

      /**
     * Verify email with token
     * GET /api/auth/verify?token=xxx
     */
    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        log.info("GET /api/auth/verify - Verifying email");
        authService.verifyEmail(token);
        return ResponseEntity.ok(MessageResponse.success("Email verified successfully. You can now login."));
    }

    /**
     * Resend verification email
     * POST /api/auth/resend-verification
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        log.info("POST /api/auth/resend-verification - Resending to: {}", request.getEmail());
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(MessageResponse.success(
                "If an account exists with this email, a verification link has been sent."));
    }

        /**
     * Request password reset
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("POST /api/auth/forgot-password - Request for: {}", request.getEmail());
        authService.forgotPassword(request.getEmail());
        // Always return success to prevent email enumeration
        return ResponseEntity.ok(MessageResponse.success(
                "If an account exists with this email, a password reset link has been sent."));
    }
     /**
     * Reset password with token
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /api/auth/reset-password - Resetting password");
        authService.resetPassword(request);
        return ResponseEntity.ok(MessageResponse.success(
                "Password reset successfully. You can now login with your new password."));
    }

    /**
     * Logout user (invalidate token)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("POST /api/auth/logout");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Instant expiryTime = jwtService.getExpiryTime(token);
                tokenBlacklistService.blacklistToken(token, expiryTime);
                log.info("User logged out successfully");
            } catch (Exception e) {
                log.debug("Could not blacklist token: {}", e.getMessage());
            }
        }

        return ResponseEntity.ok(MessageResponse.success("Logged out successfully"));
    }
}