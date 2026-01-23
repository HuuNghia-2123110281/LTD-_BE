package com.nghiashop.ecome_backend.controller;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nghiashop.ecome_backend.config.JwtUtil;
import com.nghiashop.ecome_backend.dto.Request.ForgotPasswordRequest;
import com.nghiashop.ecome_backend.dto.Request.ResetPasswordRequest;
import com.nghiashop.ecome_backend.entity.User;
import com.nghiashop.ecome_backend.repository.UserRepository;
import com.nghiashop.ecome_backend.service.EmailService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private EmailService emailService;

    // Bộ nhớ tạm lưu OTP (email -> OTP)
    private Map<String, String> otpStorage = new ConcurrentHashMap<>();

    // ============================================================
    // API 1: ĐĂNG KÝ
    // ============================================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            log.info("📝 Register attempt for email: {}", user.getEmail());
            
            // Kiểm tra email đã tồn tại
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                log.warn("⚠️ Email already exists: {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Email already exists"));
            }
            
            // Mã hóa mật khẩu
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("ROLE_USER");
            
            // Lưu user
            userRepository.save(user);
            log.info("✅ User registered successfully: {}", user.getEmail());
            
            return ResponseEntity.ok(Map.of("message", "Register success"));
            
        } catch (Exception e) {
            log.error("❌ Register error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // ============================================================
    // API 2: ĐĂNG NHẬP
    // ============================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {
        try {
            log.info("🔐 Login attempt for email: {}", request.getEmail());
            
            // Xác thực với Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), 
                            request.getPassword()
                    )
            );
            
            // Tạo JWT token
            String token = jwtUtil.generateToken(request.getEmail());
            log.info("✅ Login successful for: {}", request.getEmail());
            
            return ResponseEntity.ok(Map.of(
                    "token", token, 
                    "email", request.getEmail()
            ));
            
        } catch (BadCredentialsException e) {
            log.error("❌ Invalid credentials for: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        } catch (Exception e) {
            log.error("❌ Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // ============================================================
    // API 3: QUÊN MẬT KHẨU (Gửi OTP)
    // ============================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        try {
            log.info("🔑 Forgot password request for email: {}", request.getEmail());
            
            // 1. Kiểm tra email có tồn tại không
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                log.warn("⚠️ Email not found: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Email chưa được đăng ký trong hệ thống"));
            }

            // 2. Tạo mã OTP ngẫu nhiên (6 chữ số)
            String otpCode = String.format("%06d", new Random().nextInt(1000000));
            
            // 3. Lưu OTP vào bộ nhớ tạm (OTP sẽ đè lên OTP cũ nếu gửi lại)
            otpStorage.put(request.getEmail(), otpCode);

            // 4. IN OTP RA CONSOLE (Để xem khi test - vì Resend không gửi đến Gmail thật)
            log.info("========================================");
            log.info("🔑 OTP CODE FOR {}: {}", request.getEmail(), otpCode);
            log.info("========================================");

            // 5. GỌI SERVICE GỬI EMAIL (Async - không chặn response)
            try {
                emailService.sendResetPasswordEmail(request.getEmail(), otpCode);
                log.info("📧 Email sending triggered for: {}", request.getEmail());
            } catch (Exception e) {
                log.error("⚠️ Email send failed (but OTP is still valid): {}", e.getMessage());
                // Không throw exception - vẫn cho phép user dùng OTP từ console
            }
            
            return ResponseEntity.ok(Map.of(
                    "message", "Mã xác thực đã được gửi đến email: " + request.getEmail(),
                    "note", "Kiểm tra console logs để lấy OTP (do Resend free plan limitation)"
            ));
            
        } catch (Exception e) {
            log.error("❌ Forgot password error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // ============================================================
    // API 4: ĐẶT LẠI MẬT KHẨU (Dùng OTP)
    // ============================================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            log.info("🔓 Reset password attempt with OTP: {}", request.getToken());
            
            // 1. Tìm email dựa trên OTP
            String userEmail = null;
            for (Map.Entry<String, String> entry : otpStorage.entrySet()) {
                if (entry.getValue().equals(request.getToken())) {
                    userEmail = entry.getKey();
                    break;
                }
            }

            // 2. Kiểm tra OTP có hợp lệ không
            if (userEmail == null) {
                log.warn("⚠️ Invalid OTP: {}", request.getToken());
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Mã xác thực không hợp lệ hoặc đã hết hạn"));
            }

            // 3. Tìm user và đổi mật khẩu
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Mã hóa và lưu mật khẩu mới
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            log.info("✅ Password reset successfully for: {}", userEmail);

            // 4. Xóa OTP sau khi sử dụng (để tránh tái sử dụng)
            otpStorage.remove(userEmail);
            log.info("🗑️ OTP removed from storage for: {}", userEmail);

            return ResponseEntity.ok(Map.of(
                    "message", "Đổi mật khẩu thành công! Hãy đăng nhập lại."
            ));

        } catch (Exception e) {
            log.error("❌ Reset password error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}