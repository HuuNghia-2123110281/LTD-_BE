package com.nghiashop.ecome_backend.service;

public interface EmailService {
    void sendResetPasswordEmail(String toEmail, String resetToken);
}