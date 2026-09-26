package com.Task.Taskflow.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpServiceImpl implements OtpService {

    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;


    private static final int MAX_OTP_REQUESTS = 3;
    private static final long RATE_LIMIT_MINUTES = 10;

    public OtpServiceImpl(
            EmailService emailService,
            RedisTemplate<String, String> redisTemplate) {

        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String generateOtp() {

        int number = 100000 + new Random().nextInt(900000);

        return String.valueOf(number);
    }

    @Override
    public void sendOtp(String email) {

        if (!canSendOtp(email)) {
            throw new RuntimeException(
                    "Too many OTP requests. Please try again later."
            );
        }

        String otp = generateOtp();

        String key = "otp:" + email;

        // Store OTP for 5 minutes
        redisTemplate.opsForValue()
                .set(key, otp, 5, TimeUnit.MINUTES);

        String subject = "TaskFlow OTP Verification";

        String body = "Your TaskFlow OTP is: " + otp
                + "\n\nThis OTP is valid for 5 minutes.";

        emailService.sendEmail(email, subject, body);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {

        String key = "otp:" + email;

        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            return false;
        }

        if (storedOtp.equals(otp)) {
            redisTemplate.delete(key);
            return true;
        }

        return false;
    }

    @Override
    public boolean canSendOtp(String email) {

        String key = "otp-rate:" + email;

        String countValue = redisTemplate.opsForValue().get(key);

        if (countValue == null) {
            redisTemplate.opsForValue()
                    .set(key, "1", RATE_LIMIT_MINUTES, TimeUnit.MINUTES);

            return true;
        }

        int count = Integer.parseInt(countValue);

        if (count >= MAX_OTP_REQUESTS) {
            return false;
        }

        redisTemplate.opsForValue().increment(key);

        return true;
    }
}