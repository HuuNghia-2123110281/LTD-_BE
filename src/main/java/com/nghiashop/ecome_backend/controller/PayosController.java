package com.nghiashop.ecome_backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nghiashop.ecome_backend.entity.Order;
import com.nghiashop.ecome_backend.repository.OrderRepository;
import com.nghiashop.ecome_backend.service.PayosService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PayosController {

    private final PayosService payosService;
    private final OrderRepository orderRepository;

    // Map tạm để lưu mapping giữa orderCode và orderId
    private final Map<Long, Long> orderCodeMap = new HashMap<>();

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody CreatePaymentDto dto) {
        try {
            System.out.println("📥 Tạo thanh toán: orderId=" + dto.orderId + ", amount=" + dto.amount);

            Order order = orderRepository.findById(dto.orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy order #" + dto.orderId));

            // Kiểm tra trạng thái đơn hàng
            if (!"PENDING".equals(order.getStatus())) {
                throw new RuntimeException("Đơn hàng không ở trạng thái PENDING");
            }

            // Tạo orderCode duy nhất
            Long orderCode = System.currentTimeMillis() / 1000;

            // Lưu mapping orderCode -> orderId
            orderCodeMap.put(orderCode, order.getId());

            // Tạo description (giới hạn 25 ký tự)
            String description = String.format("Order #%d", order.getId());
            if (description.length() > 25) {
                description = description.substring(0, 25);
            }

            // Gọi PayOS API
            Map<String, Object> resp = payosService.createPaymentLink(
                    orderCode,
                    dto.amount != null ? dto.amount : order.getTotalPrice(),
                    description,
                    dto.returnUrl != null ? dto.returnUrl : "myapp://payment-return",
                    dto.cancelUrl != null ? dto.cancelUrl : "myapp://payment-cancel",
                    dto.expiredAt);

            System.out.println("✅ PayOS response: " + resp);

            if (resp == null) {
                throw new RuntimeException("PayOS trả về response null");
            }

            String code = resp.get("code") != null ? resp.get("code").toString() : null;
            if (!"00".equals(code)) {
                String errorMsg = (String) resp.get("desc");
                throw new RuntimeException("PayOS error: " + errorMsg);
            }

            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            if (data == null) {
                throw new RuntimeException("PayOS response không có trường data");
            }

            String checkoutUrl = (String) data.get("checkoutUrl");
            if (checkoutUrl == null || checkoutUrl.isEmpty()) {
                throw new RuntimeException("PayOS không trả về checkoutUrl");
            }

            // ===== LẤY QR CODE TỪ PAYOS =====
            Long amount = dto.amount != null ? dto.amount : order.getTotalPrice();
            
            // PayOS trả về QR code dạng EMVCo (text string)
            String qrCodeString = (String) data.get("qrCode");
            
            if (qrCodeString == null || qrCodeString.isEmpty()) {
                throw new RuntimeException("PayOS không trả về QR code");
            }
            
            // Convert EMVCo string thành URL hình ảnh bằng API QR generator
            String qrCodeUrl = String.format(
                "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=%s",
                java.net.URLEncoder.encode(qrCodeString, "UTF-8")
            );
            
            System.out.println("🔗 QR Code EMVCo: " + qrCodeString.substring(0, Math.min(50, qrCodeString.length())) + "...");
            System.out.println("🔗 QR Code URL: " + qrCodeUrl);
            // ====================================
            
            // Trả response cho client
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentUrl", checkoutUrl);
            response.put("qrCode", qrCodeUrl); // QR code URL
            response.put("orderCode", orderCode);
            response.put("orderId", dto.orderId);
            response.put("amount", amount);

            System.out.println("✅ Response to client: " + response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> webhookData) {
        try {
            System.out.println("🔔 Nhận webhook từ PayOS: " + webhookData);

            Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
            Long orderCode = Long.parseLong(data.get("orderCode").toString());
            String status = (String) data.get("status");
            String transactionId = data.get("id").toString();
            Long amount = Long.parseLong(data.get("amount").toString());

            System.out.println("📋 Webhook: orderCode=" + orderCode + ", status=" + status);

            // Tìm orderId từ orderCode
            Long orderId = orderCodeMap.get(orderCode);
            if (orderId == null) {
                System.err.println("❌ Không tìm thấy order với orderCode: " + orderCode);
                return ResponseEntity.status(404).body(Map.of("error", "Order not found"));
            }

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy order #" + orderId));

            if ("PAID".equals(status)) {
                order.setStatus("PAID");
                orderRepository.save(order);
                System.out.println("✅ Đã cập nhật status=PAID cho order #" + order.getId());
                
            } else if ("CANCELLED".equals(status)) {
                order.setStatus("CANCELLED");
                orderRepository.save(order);
                System.out.println("❌ Thanh toán bị hủy cho order #" + order.getId());
            }

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            System.err.println("❌ Lỗi xử lý webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/verify/{orderCode}")
    public ResponseEntity<?> verifyPayment(@PathVariable Long orderCode) {
        try {
            System.out.println("🔍 Verify payment: " + orderCode);

            // Tìm orderId từ orderCode
            Long orderId = orderCodeMap.get(orderCode);
            if (orderId == null) {
                System.err.println("❌ Không tìm thấy orderCode trong map: " + orderCode);
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "error", "Không tìm thấy order"));
            }

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy order"));

            boolean isPaid = false;
            
            try {
                // Lấy trạng thái từ PayOS
                Map<String, Object> payosStatus = payosService.getPaymentStatus(orderCode);
                
                System.out.println("📥 PayOS verify response: " + payosStatus);

                if (payosStatus != null && "00".equals(payosStatus.get("code"))) {
                    Map<String, Object> data = (Map<String, Object>) payosStatus.get("data");
                    String status = (String) data.get("status");
                    
                    System.out.println("📊 Payment status from PayOS: " + status);

                    if ("PAID".equals(status)) {
                        isPaid = true;

                        // Cập nhật nếu chưa được cập nhật
                        if (!"PAID".equals(order.getStatus())) {
                            order.setStatus("PAID");
                            orderRepository.save(order);
                            System.out.println("✅ Đã cập nhật status=PAID cho order #" + order.getId());
                        }
                    }
                }
            } catch (Exception e) {
                // Nếu PayOS API lỗi, vẫn trả về status hiện tại của order
                System.err.println("⚠️ Lỗi khi gọi PayOS API: " + e.getMessage());
                // Check xem order đã PAID chưa
                if ("PAID".equals(order.getStatus())) {
                    isPaid = true;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", order.getId());
            response.put("orderCode", orderCode);
            response.put("isPaid", isPaid);
            response.put("status", order.getStatus());
            response.put("totalPrice", order.getTotalPrice());
            response.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "");

            System.out.println("✅ Verify response: isPaid=" + isPaid + ", status=" + order.getStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error verifying payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    @GetMapping("/history/{orderId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy order"));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderId", orderId,
                    "status", order.getStatus(),
                    "totalPrice", order.getTotalPrice(),
                    "paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "",
                    "createdAt", order.getCreatedAt()));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    public static class CreatePaymentDto {
        public Long orderId;
        public Long amount; 
        public String returnUrl;
        public String cancelUrl;
        public Integer expiredAt;
    }
}