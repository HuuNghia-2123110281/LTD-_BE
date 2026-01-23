package com.nghiashop.ecome_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nghiashop.ecome_backend.service.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final Resend resend;
    
    // Đọc từ application.properties
    @Value("${resend.from.email:onboarding@resend.dev}")
    private String fromEmail;

    @Override
    public void sendResetPasswordEmail(String toEmail, String resetToken) {
        try {
            log.info("🚀 Starting to send reset password email to: {}", toEmail);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail) // Dùng email từ config
                    .to(toEmail)
                    .subject("🔐 Đặt lại mật khẩu - NghiaShop Laptop") 
                    .html(buildEmailTemplate(resetToken))
                    .build();

            CreateEmailResponse data = resend.emails().send(params);
            log.info("✅ Email sent successfully! Email ID: {} | Recipient: {}", 
                     data.getId(), toEmail);
            
        } catch (ResendException e) {
            log.error("❌ Failed to send email to: {} | Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }

    private String buildEmailTemplate(String resetToken) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "<div style='background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%); padding: 30px; border-radius: 10px 10px 0 0;'>"
                + "<h1 style='color: white; margin: 0; text-align: center; font-size: 24px;'>💻 NghiaShop Laptop</h1>"
                + "</div>"
                + "<div style='background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; border: 1px solid #e5e7eb; border-top: none;'>"
                + "<h2 style='color: #1f2937; margin-top: 0;'>Đặt lại mật khẩu</h2>"
                + "<p style='color: #4b5563; font-size: 16px;'>Xin chào,</p>"
                + "<p style='color: #4b5563; font-size: 16px;'>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản mua sắm tại NghiaShop.</p>"
                + "<div style='background: white; padding: 25px; border-radius: 8px; margin: 25px 0; text-align: center; border: 2px dashed #3b82f6;'>"
                + "<p style='color: #6b7280; font-size: 14px; margin: 0 0 10px 0;'>Mã xác thực của bạn là:</p>"
                + "<h1 style='color: #1e3a8a; font-size: 36px; letter-spacing: 8px; margin: 10px 0; font-family: monospace; font-weight: bold;'>"
                + resetToken + "</h1>"
                + "</div>"
                + "<div style='background: #fef3c7; padding: 15px; border-radius: 8px; border-left: 4px solid #f59e0b;'>"
                + "<p style='color: #92400e; margin: 0; font-size: 14px;'>⏰ <strong>Lưu ý:</strong> Mã này có hiệu lực trong 15 phút. Vì lý do bảo mật, vui lòng không chia sẻ mã này.</p>"
                + "</div>"
                + "<p style='color: #6b7280; font-size: 14px; margin-top: 25px;'>Nếu bạn không yêu cầu thay đổi, vui lòng bỏ qua email này.</p>"
                + "<hr style='border: none; border-top: 1px solid #e5e7eb; margin: 25px 0;'>"
                + "<p style='color: #9ca3af; font-size: 12px; text-align: center;'>Trân trọng,<br><strong>NghiaShop Team</strong><br>Chuyên Laptop & Phụ kiện chính hãng</p>"
                + "</div>"
                + "</div>";
    }
}