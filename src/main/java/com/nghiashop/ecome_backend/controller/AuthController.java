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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nghiashop.ecome_backend.config.JwtUtil;
import com.nghiashop.ecome_backend.entity.User;
import com.nghiashop.ecome_backend.repository.UserRepository;
import com.nghiashop.ecome_backend.service.EmailService;

class OtpRequest {
    public String email;
}

class ResetPasswordRequest {
    public String email;
    public String otp;
    public String newPassword;
}

@RestController
@RequestMapping("/api/auth")
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

    // Bộ nhớ tạm lưu OTP (Key: Email, Value: Mã OTP)
    private Map<String, String> otpStorage = new ConcurrentHashMap<>();

    // --- API 1: GỬI OTP (PHIÊN BẢN MOCK - IN RA LOGS) ---
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        // 1. Kiểm tra email có tồn tại không
        Optional<User> userOptional = userRepository.findByEmail(request.email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Email chưa được đăng ký trong hệ thống"));
        }

        // 2. Tạo mã OTP ngẫu nhiên (6 chữ số)
        String otpCode = String.valueOf(new Random().nextInt(900000) + 100000);
        
        // 3. Lưu OTP vào bộ nhớ tạm
        otpStorage.put(request.email, otpCode);

        // 4. GIẢ LẬP GỬI MAIL (IN RA MÀN HÌNH LOGS)
        // Dùng Thread để không làm chậm App
        new Thread(() -> {
            try {
                System.out.println("⏳ [Background] Đang xử lý yêu cầu gửi OTP...");
                
                // --- ĐOẠN NÀY QUAN TRỌNG: IN MÃ OTP RA LOGS ---
                System.out.println("======================================================");
                System.out.println("🔥 [CHẾ ĐỘ TEST] MÃ OTP CHO EMAIL " + request.email + " LÀ: " + otpCode);
                System.out.println("🔥 Hãy copy mã này nhập vào App!");
                System.out.println("======================================================");

                // Tạm thời comment dòng gửi mail thật để tránh lỗi Connection Timed Out
                // emailService.sendEmail(request.email, "Subject", "Content"); 
                
            } catch (Exception e) {
                System.err.println("❌ Lỗi hệ thống: " + e.getMessage());
            }
        }).start();

        // 5. Trả về thành công ngay lập tức (Để App chuyển màn hình nhập OTP)
        return ResponseEntity.ok(Map.of("message", "Mã xác thực đã được gửi (Check Logs Railway)!"));
    }

    // --- API 2: ĐỔI MẬT KHẨU (GIỮ NGUYÊN) ---
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            // 1. Kiểm tra OTP
            String savedOtp = otpStorage.get(request.email);
            
            if (savedOtp == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng yêu cầu gửi mã OTP trước"));
            }
            
            // So sánh OTP người dùng nhập với OTP trong Server
            if (!savedOtp.equals(request.otp)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã OTP không chính xác"));
            }

            // 2. Tìm user và đổi mật khẩu
            Optional<User> userOptional = userRepository.findByEmail(request.email);
            if (userOptional.isEmpty()) return ResponseEntity.badRequest().build();

            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(request.newPassword));
            userRepository.save(user);

            // 3. Xóa OTP sau khi dùng xong
            otpStorage.remove(request.email);

            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công! Hãy đăng nhập lại."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // ... (Giữ nguyên API Register)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Email already exists"));
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("ROLE_USER");
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Register success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // ... (Giữ nguyên API Login)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            String token = jwtUtil.generateToken(request.getEmail());
            return ResponseEntity.ok(Map.of("token", token, "email", request.getEmail()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
        }
    }
}