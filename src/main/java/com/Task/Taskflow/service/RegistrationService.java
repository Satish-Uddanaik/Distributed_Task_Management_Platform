package com.Task.Taskflow.service;


import com.Task.Taskflow.dto.RegisterRequest;

public interface RegistrationService {

    void register(RegisterRequest request);

    void verifyRegistration(String email, String otp);
}
