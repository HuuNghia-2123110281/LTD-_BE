package com.nghiashop.ecome_backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nghiashop.ecome_backend.dto.OrderDTO;
import com.nghiashop.ecome_backend.dto.OrderItemDTO;
import com.nghiashop.ecome_backend.entity.Order;
import com.nghiashop.ecome_backend.entity.OrderItem;
import com.nghiashop.ecome_backend.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<OrderDTO> getAll() {
        List<Order> orders = orderService.getAll();
        
        // Map sang DTO để trả về đầy đủ thông tin
        return orders.stream()
                .map(order -> {
                    OrderDTO dto = new OrderDTO();
                    dto.setId(order.getId());
                    dto.setTotalPrice(order.getTotalPrice());
                    dto.setStatus(order.getStatus());
                    dto.setPaymentMethod(order.getPaymentMethod());
                    dto.setCreatedAt(order.getCreatedAt());
                    
                    // Map OrderItems với thông tin sản phẩm đầy đủ
                    if (order.getItems() != null) {
                        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                                .map(item -> new OrderItemDTO(
                                        item.getId(),
                                        item.getProduct().getId(),
                                        item.getProduct().getName(),
                                        item.getProduct().getImage(),
                                        item.getQuantity(),
                                        item.getPrice()))
                                .collect(Collectors.toList());
                        dto.setItems(itemDTOs);
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());

        // Map OrderItems với thông tin sản phẩm đầy đủ
        if (order.getItems() != null) {
            List<OrderItemDTO> itemDTOs = order.getItems().stream()
                    .map(item -> new OrderItemDTO(
                            item.getId(),
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            item.getProduct().getImage(),
                            item.getQuantity(),
                            item.getPrice()))
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("PENDING");
        
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
            }
        }

        Order savedOrder = orderService.create(order);
        return ResponseEntity.ok(savedOrder);
    }
}