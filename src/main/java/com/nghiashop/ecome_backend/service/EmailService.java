package com.nghiashop.ecome_backend.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // XÓA HOẶC COMMENT DÒNG NÀY ĐI
    // @Autowired
    // private JavaMailSender emailSender; 

    public void sendEmail(String to, String subject, String text) {
        // Thay vì gửi mail thật (dễ gây lỗi nếu cấu hình sai), ta chỉ in ra màn hình
        System.out.println("================ EMAIL MOCK ================");
        System.out.println("Gửi đến: " + to);
        System.out.println("Tiêu đề: " + subject);
        System.out.println("Nội dung: " + text);
        System.out.println("============================================");
    }
}