package com.nghiashop.ecome_backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nghiashop.ecome_backend.entity.Order;
import com.nghiashop.ecome_backend.entity.OrderItem;
import com.nghiashop.ecome_backend.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<Order> getAll() {
        return orderService.getAll();
    }
    
    // API lấy chi tiết đơn hàng (Dùng để App kiểm tra trạng thái)
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        // Lưu ý: Bạn cần thêm hàm findById trong OrderService nếu chưa có.
        // Hoặc dùng trực tiếp Repository nếu muốn nhanh: orderRepository.findById(id)
        // Ở đây mình giả sử Service đã có hàm getById hoặc findById
        Order order = orderService.getById(id); 
        if (order != null) {
             return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        order.setCreatedAt(LocalDateTime.now());
        
        // Luôn set trạng thái ban đầu là PENDING
        order.setStatus("PENDING");

        // Liên kết OrderItem với Order
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
            }
        }

        Order savedOrder = orderService.create(order);
        return ResponseEntity.ok(savedOrder);
    }
}