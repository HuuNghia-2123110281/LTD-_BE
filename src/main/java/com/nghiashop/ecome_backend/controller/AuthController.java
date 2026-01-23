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
// Import đúng các DTO đã tạo
import com.nghiashop.ecome_backend.dto.Request.ForgotPasswordRequest;
import com.nghiashop.ecome_backend.dto.Request.ResetPasswordRequest;
import com.nghiashop.ecome_backend.entity.User;
import com.nghiashop.ecome_backend.repository.UserRepository;
import com.nghiashop.ecome_backend.service.EmailService;

import jakarta.validation.Valid;

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

    // Bộ nhớ tạm lưu OTP
    private Map<String, String> otpStorage = new ConcurrentHashMap<>();

    // --- API 1: QUÊN MẬT KHẨU (Gửi Email thật qua Resend) ---
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        // 1. Kiểm tra email có tồn tại không
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Email chưa được đăng ký trong hệ thống"));
        }

        // 2. Tạo mã OTP ngẫu nhiên (6 chữ số)
        String otpCode = String.valueOf(new Random().nextInt(900000) + 100000);
        
        // 3. Lưu OTP vào bộ nhớ tạm (OTP sẽ đè lên OTP cũ nếu gửi lại)
        otpStorage.put(request.getEmail(), otpCode);

        // 4. GỌI SERVICE GỬI MAIL
        try {
            emailService.sendResetPasswordEmail(request.getEmail(), otpCode);
            return ResponseEntity.ok(Map.of("message", "Mã xác thực đã được gửi đến email: " + request.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi gửi email: " + e.getMessage()));
        }
    }

    // --- API 2: ĐỔI MẬT KHẨU (Dùng Token/OTP để xác thực) ---
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            // 1. Tìm Email dựa trên OTP (Token) được gửi lên
            // Lưu ý: Cách này duyệt Map hơi thủ công, nhưng ổn với quy mô nhỏ
            String userEmail = null;
            for (Map.Entry<String, String> entry : otpStorage.entrySet()) {
                if (entry.getValue().equals(request.getToken())) {
                    userEmail = entry.getKey();
                    break;
                }
            }

            if (userEmail == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã xác thực không hợp lệ hoặc đã hết hạn"));
            }

            // 2. Tìm user và đổi mật khẩu
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // 3. Xóa OTP sau khi dùng xong để tránh dùng lại
            otpStorage.remove(userEmail);

            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công! Hãy đăng nhập lại."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // --- API ĐĂNG KÝ (Giữ nguyên) ---
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

    // --- API ĐĂNG NHẬP (Giữ nguyên) ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {
        try {
            // Authentication manager sẽ ném ngoại lệ nếu sai pass
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            
            String token = jwtUtil.generateToken(request.getEmail());
            return ResponseEntity.ok(Map.of("token", token, "email", request.getEmail()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
        }
    }
}