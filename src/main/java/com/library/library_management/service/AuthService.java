package com.library.library_management.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.library.library_management.dto.request.LoginRequest;
import com.library.library_management.dto.request.RegisterRequest;
import com.library.library_management.dto.request.ResetPasswordRequest;
import com.library.library_management.dto.response.AuthResponse;
import com.library.library_management.entity.User;
import com.library.library_management.entity.VerificationToken;
import com.library.library_management.entity.enums.Role;
import com.library.library_management.entity.enums.TokenType;
import com.library.library_management.exception.ResourceNotFoundException;
import com.library.library_management.repository.UserRepository;
import com.library.library_management.security.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
     private final TokenService tokenService;
    private final EmailService emailService;

//     public AuthResponse register(RegisterRequest request)
//     {
//        log.info("Registering new user with email: {}", request.getEmail());

//         // Check if email already exists
//         if (userRepository.existsByEmail(request.getEmail())) {
//             throw new IllegalArgumentException("Email already registered");
//         }
//         // Create new user
//         User user = User.builder()
//                 .name(request.getName())
//                 .email(request.getEmail())
//                 .password(passwordEncoder.encode(request.getPassword()))
//                 .role(Role.USER)
//                 .build();

//                 User savedUser = userRepository.save(user);
//         log.info("User registered successfully with id: {}", savedUser.getId());

//         // Generate JWT token per connection automatik ne library
//         String token = jwtService.generateToken(
//                 savedUser.getId(),
//                 savedUser.getEmail(),
//                 savedUser.getRole().name()
//         );

//         return buildAuthResponse(savedUser, token);
//     }
@Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create new user (unverified by default)
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
              .role(Role.USER)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        // Create verification token and send email
        String verificationToken = tokenService.createEmailVerificationToken(savedUser);
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getName(), verificationToken);

        // Generate JWT token (user can login but will have limited access until verified)
        String jwtToken = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return buildAuthResponse(savedUser, jwtToken);
    }





    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (!user.getEmailVerified()) {
            log.warn("Login attempt with unverified email: {}", request.getEmail());
            throw new IllegalArgumentException("Please verify your email before logging in. Check your inbox for the verification link.");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        // Generate JWT token
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return buildAuthResponse(user, token);
    }
 @Transactional
    public void verifyEmail(String token) {
        log.info("Verifying email with token");

        Optional<VerificationToken> verificationToken = tokenService.validateToken(token, TokenType.EMAIL_VERIFICATION);

        if (verificationToken.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired verification token");
        }

        VerificationToken vt = verificationToken.get();
        User user = vt.getUser();

        if (user.getEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }
        user.setEmailVerified(true);
        userRepository.save(user);

        // Mark token as used
        tokenService.markTokenAsUsed(vt);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        log.info("Email verified for user: {}", user.getEmail());
    }
     @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Resending verification email to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email"));

        if (user.getEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        // Create new verification token and send email
        String verificationToken = tokenService.createEmailVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationToken);

        log.info("Verification email resent to: {}", email);
    }
    @Transactional
    public void forgotPassword(String email) {
        log.info("Password reset requested for: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);

        // Always return success to prevent email enumeration
        if (userOptional.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return; // Don't reveal that email doesn't exist
        }

        User user = userOptional.get();

        // Create password reset token and send email
        String resetToken = tokenService.createPasswordResetToken(user);
        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetToken);

        log.info("Password reset email sent to: {}", email);
    }

      @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password with token");

        Optional<VerificationToken> verificationToken = tokenService.validateToken(
                request.getToken(), TokenType.PASSWORD_RESET);

        if (verificationToken.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired password reset token");
        }

        VerificationToken vt = verificationToken.get();
           User user = vt.getUser();

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        tokenService.markTokenAsUsed(vt);

        // Send confirmation email
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getName());

        log.info("Password reset successful for user: {}", user.getEmail());
    }


    /// We will use for Session Persistence per react sepse reacti humb memorjen 
    /// JwtTokenin nuk ka ka info si name dhe te tjera kshtu qe na duhet
    /// also for different updates such as User to Admin 
    public AuthResponse.UserInfo getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .role(user.getRole())
                        .build())
                .build();
    }
}
