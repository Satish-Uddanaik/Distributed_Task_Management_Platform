package com.Task.Taskflow.service;

import com.Task.Taskflow.dto.RegisterRequest;
import com.Task.Taskflow.entity.Role;
import com.Task.Taskflow.entity.User;
import com.Task.Taskflow.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final OtpService otpService;

    public RegistrationServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RedisTemplate<String, String> redisTemplate,
            OtpService otpService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.otpService = otpService;
    }

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        String key = "registration:" + request.getEmail();

        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        redisTemplate.opsForHash().put(key, "name", request.getName());
        redisTemplate.opsForHash().put(key, "email", request.getEmail());
        redisTemplate.opsForHash().put(key, "password", encodedPassword);

        redisTemplate.expire(key, 5, TimeUnit.MINUTES);

        otpService.sendOtp(request.getEmail());
    }

    @Override
    public void verifyRegistration(String email, String otp) {

        boolean valid = otpService.verifyOtp(email, otp);

        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        String key = "registration:" + email;

        Object name = redisTemplate.opsForHash().get(key, "name");
        Object password = redisTemplate.opsForHash().get(key, "password");

        if (name == null || password == null) {
            throw new RuntimeException(
                    "Registration data expired. Please register again."
            );
        }

        User user = User.builder()
                .name(name.toString())
                .email(email)
                .password(password.toString())
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .emailVerified(true)
                .build();

        userRepository.save(user);

        redisTemplate.delete(key);
    }
}