package com.nghiashop.ecome_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    // THÊM CHỮ "throws Exception" VÀO SAU TÊN HÀM
    public void sendEmail(String to, String subject, String text) throws Exception {
        // XÓA TRY-CATCH ĐI, ĐỂ LỖI BẮN THẲNG RA NGOÀI
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("NghiaShop Support <nghia03052004@gmail.com>");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        emailSender.send(message);
        System.out.println("✅ Đã gửi email thành công đến: " + to);
    }
}