package com.nghiashop.ecome_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        // Thay bằng email của bạn hoặc tên hiển thị mong muốn
        message.setFrom("NghiaShop@support.com"); 
        message.setTo(to);
        message.setSubject(subject);
        message.setTo(text);
        emailSender.send(message);
        System.out.println("Đã gửi email đến: " + to);
    }
}