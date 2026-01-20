package com.nghiashop.ecome_backend.controller;

import java.util.HashMap;
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

    private Map<String, String> otpStorage = new ConcurrentHashMap<>();

    // --- API 1: GỬI OTP (CHẾ ĐỘ DEBUG - HIỆN LỖI) ---
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        // 1. Kiểm tra email
        Optional<User> userOptional = userRepository.findByEmail(request.email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Email chưa được đăng ký"));
        }

        // 2. Tạo OTP
        String otpCode = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStorage.put(request.email, otpCode);

        // 3. Gửi Email (BỎ THREAD ĐI ĐỂ TEST LỖI)
        try {
            System.out.println("⏳ Đang thử gửi email tới: " + request.email);
            
            String subject = "Mã xác thực quên mật khẩu - NghiaShop";
            String content = "Xin chào " + userOptional.get().getFullName() + ",\n\n" +
                             "Mã OTP xác thực của bạn là: " + otpCode + "\n" +
                             "Mã này có hiệu lực trong 5 phút.\n\n" +
                             "Trân trọng,\nNghiaShop Team.";
            
            // Gọi trực tiếp: Nếu lỗi dòng này sẽ nhảy xuống catch ngay
            emailService.sendEmail(request.email, subject, content);
            
            System.out.println("✅ Gửi thành công!");

        } catch (Exception e) {
            // IN LỖI RA CONSOLE CHO BẠN THẤY
            System.err.println("❌ LỖI GỬI MAIL CHI TIẾT: " + e.getMessage());
            e.printStackTrace();

            // TRẢ VỀ LỖI CHO APP THẤY
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi gửi mail: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", "Mã xác thực đã được gửi tới email!"));
    }

    // --- API 2: ĐỔI MẬT KHẨU ---
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            String savedOtp = otpStorage.get(request.email);
            
            if (savedOtp == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng yêu cầu gửi mã OTP trước"));
            }
            
            if (!savedOtp.equals(request.otp)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã OTP không chính xác"));
            }

            Optional<User> userOptional = userRepository.findByEmail(request.email);
            if (userOptional.isEmpty()) return ResponseEntity.badRequest().build();

            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(request.newPassword));
            userRepository.save(user);
            otpStorage.remove(request.email);

            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công! Hãy đăng nhập lại."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // ... (Giữ nguyên register/login)
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