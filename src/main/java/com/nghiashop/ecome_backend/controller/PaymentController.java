package com.nghiashop.ecome_backend.controller;

import com.nghiashop.ecome_backend.entity.Order;
import com.nghiashop.ecome_backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private OrderRepository orderRepository;

    // API xác nhận thanh toán (Giả lập Webhook ngân hàng)
    @PutMapping("/confirm/{orderId}")
    public ResponseEntity<?> confirmPayment(@PathVariable Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus("PAID"); // Đổi trạng thái thành Đã thanh toán
            orderRepository.save(order);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Thanh toán thành công");
            response.put("status", "PAID");
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng");
        }
    }
}