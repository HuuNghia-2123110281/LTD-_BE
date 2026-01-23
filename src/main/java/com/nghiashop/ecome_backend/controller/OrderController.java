package com.nghiashop.ecome_backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.nghiashop.ecome_backend.dto.OrderDTO;
import com.nghiashop.ecome_backend.dto.OrderItemDTO;
import com.nghiashop.ecome_backend.entity.Order;
import com.nghiashop.ecome_backend.entity.OrderItem;
import com.nghiashop.ecome_backend.entity.User;
import com.nghiashop.ecome_backend.service.OrderService;
import com.nghiashop.ecome_backend.service.impl.OrderServiceImpl;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderServiceImpl orderService; // Đổi sang OrderServiceImpl để có thể gọi getAllByUser

    // Lấy tất cả đơn hàng của user đang đăng nhập
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getMyOrders(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).build(); // Unauthorized
            }
            
            List<OrderDTO> orderDTOs = orderService.getAllByUser(user);
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Lấy chi tiết 1 đơn hàng
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id, 
                                                  @AuthenticationPrincipal User user) {
        try {
            Order order = orderService.getById(id);
            
            if (order == null) {
                return ResponseEntity.notFound().build();
            }

            // Kiểm tra quyền truy cập - chỉ user sở hữu mới xem được
            if (user == null || !order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).build(); // Forbidden
            }

            OrderDTO dto = convertToDTO(order);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order, 
                                       @AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            order.setUser(user); // Set user cho order
            order.setCreatedAt(LocalDateTime.now());
            order.setStatus("PENDING");

            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    item.setOrder(order);
                }
            }

            Order savedOrder = orderService.create(order);
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Helper method để chuyển Order -> OrderDTO
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemDTO> itemDTOs = order.getItems().stream()
            .map(item -> new OrderItemDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getImage(),
                item.getQuantity(),
                item.getPrice()
            ))
            .collect(Collectors.toList());

        dto.setItems(itemDTOs);
        return dto;
    }
}