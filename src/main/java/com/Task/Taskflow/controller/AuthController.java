package com.Task.Taskflow.controller;

import com.Task.Taskflow.dto.AuthRequest;
import com.Task.Taskflow.dto.AuthResponse;
import com.Task.Taskflow.service.AuthService;
import com.Task.Taskflow.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    public AuthController(AuthService authService, EmailService emailService) {
        this.authService = authService;
        this.emailService = emailService;
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
}