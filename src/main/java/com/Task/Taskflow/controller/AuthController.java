package com.Task.Taskflow.controller;

import com.Task.Taskflow.dto.AuthRequest;
import com.Task.Taskflow.dto.AuthResponse;
import com.Task.Taskflow.dto.RegisterRequest;
import com.Task.Taskflow.dto.ResetPasswordRequest;
import com.Task.Taskflow.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final OtpService otpService;
    private final RegistrationService registrationService;
    private final ForgotPasswordService forgotPasswordService;

    public AuthController(AuthService authService, EmailService emailService, OtpService otpService, RegistrationService registrationService,
                          ForgotPasswordService forgotPasswordService) {
        this.authService = authService;
        this.emailService = emailService;
        this.otpService = otpService;
        this.registrationService=registrationService;
        this.forgotPasswordService=forgotPasswordService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        registrationService.register(request);

        return ResponseEntity.ok(
                "OTP sent to your email"
        );
    }

    @PostMapping("/register/verify")
    public ResponseEntity<String> verifyRegistration(
            @RequestParam String email,
            @RequestParam String otp) {

        registrationService.verifyRegistration(email, otp);

        return ResponseEntity.ok(
                "Registration successful"
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail(
            @RequestParam String email) {

        emailService.sendEmail(
                email,
                "TaskFlow Test Email",
                "Hello! This email was sent from TaskFlow."
        );

        return ResponseEntity.ok("Email sent successfully");
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(
            @RequestParam String email) {

        otpService.sendOtp(email);

        return ResponseEntity.ok("OTP sent successfully");
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        boolean valid = otpService.verifyOtp(email, otp);

        if (!valid) {
            return ResponseEntity.badRequest()
                    .body("Invalid or expired OTP");
        }

        return ResponseEntity.ok("OTP verified successfully");
    }

    @PostMapping("/register/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestParam String email) {

        otpService.sendOtp(email);

        return ResponseEntity.ok("OTP resent successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestParam String email) {

        forgotPasswordService.sendForgotPasswordOtp(email);

        return ResponseEntity.ok(
                "OTP sent to your email");
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<String> verifyForgotPasswordOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        boolean valid =
                forgotPasswordService.verifyForgotPasswordOtp(
                        email, otp);

        if (!valid) {
            return ResponseEntity.badRequest()
                    .body("Invalid or expired OTP");
        }

        return ResponseEntity.ok(
                "OTP verified successfully");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String email,
            @Valid @RequestBody ResetPasswordRequest request) {

        forgotPasswordService.resetPassword(
                email,
                request.getNewPassword());

        return ResponseEntity.ok(
                "Password reset successfully");
    }
}