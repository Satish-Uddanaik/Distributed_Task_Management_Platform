package com.Task.Taskflow.service;

public interface OtpService {

    String generateOtp();

    void sendOtp(String email);

    boolean verifyOtp(String email,String otp);

    boolean canSendOtp(String email);
}