package com.nghiashop.ecome_backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
import com.nghiashop.ecome_backend.dto.ResetPasswordRequest;
import com.nghiashop.ecome_backend.entity.User;
import com.nghiashop.ecome_backend.repository.UserRepository;
import com.nghiashop.ecome_backend.service.EmailService; // <--- Import Service gửi mail

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

    // Inject EmailService để dùng chức năng gửi mail
    @Autowired
    private EmailService emailService; 

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Email already exists"));
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("ROLE_USER");
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Register success"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            String token = jwtUtil.generateToken(request.getEmail());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("email", request.getEmail());

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Login failed: " + e.getMessage()));
        }
    }

    // --- API: QUÊN MẬT KHẨU (CÓ GỬI EMAIL) ---
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            // 1. Tìm user theo email
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

            if (userOptional.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Email không tồn tại trong hệ thống"));
            }

            User user = userOptional.get();

            // 2. Mã hóa mật khẩu mới và lưu vào DB
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // 3. Gửi Email thông báo (Bọc try-catch để lỗi gửi mail không làm lỗi cả request)
            try {
                String subject = "Thông báo thay đổi mật khẩu - NghiaShop";
                String text = "Xin chào " + (user.getFullName() != null ? user.getFullName() : "bạn") + ",\n\n" +
                              "Mật khẩu của bạn đã được thay đổi thành công.\n" +
                              "Nếu bạn không thực hiện thao tác này, vui lòng liên hệ admin ngay lập tức.\n\n" +
                              "Trân trọng,\nNghiaShop Team.";
                
                emailService.sendEmail(user.getEmail(), subject, text);
            } catch (Exception e) {
                System.err.println("Lỗi gửi mail: " + e.getMessage());
                // Vẫn cho return OK vì mật khẩu đã đổi thành công, chỉ là mail không gửi được
            }

            // 4. Trả về thành công
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công! Vui lòng kiểm tra email."));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi đổi mật khẩu: " + e.getMessage()));
        }
    }
}