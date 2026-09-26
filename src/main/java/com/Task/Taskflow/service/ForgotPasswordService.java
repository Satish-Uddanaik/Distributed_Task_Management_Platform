package com.Task.Taskflow.service;

public interface ForgotPasswordService {

    void sendForgotPasswordOtp(String email);

    boolean verifyForgotPasswordOtp(String email, String otp);

    void resetPassword(String email, String newPassword);
}