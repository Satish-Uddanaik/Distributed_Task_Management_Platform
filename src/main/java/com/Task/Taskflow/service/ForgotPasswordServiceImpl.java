package com.Task.Taskflow.service;

import com.Task.Taskflow.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.Task.Taskflow.entity.User;

import java.util.concurrent.TimeUnit;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final RedisTemplate<String, String> redisTemplate;

    public ForgotPasswordServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService,
            RedisTemplate<String, String> redisTemplate) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void sendForgotPasswordOtp(String email) {

        userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        otpService.sendOtp(email);
    }

    @Override
    public boolean verifyForgotPasswordOtp(String email, String otp) {

        boolean valid = otpService.verifyOtp(email, otp);

        if (!valid) {
            return false;
        }

        String key = "forgot-password-verified:" + email;

        redisTemplate.opsForValue()
                .set(key, "true", 10, TimeUnit.MINUTES);

        return true;
    }

    @Override
    public void resetPassword(String email, String newPassword) {

        String key = "forgot-password-verified:" + email;

        String verified = redisTemplate.opsForValue().get(key);

        if (!"true".equals(verified)) {
            throw new RuntimeException(
                    "Please verify OTP before resetting password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String encodedPassword =
                passwordEncoder.encode(newPassword);

        user.setPassword(encodedPassword);

        userRepository.save(user);

        redisTemplate.delete(key);
    }
}