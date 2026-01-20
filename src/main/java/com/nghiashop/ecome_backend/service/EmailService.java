package com.nghiashop.ecome_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("NghiaShop Support <nghia03052004@gmail.com>");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            emailSender.send(message);
            System.out.println("✅ Đã gửi email thành công đến: " + to);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}