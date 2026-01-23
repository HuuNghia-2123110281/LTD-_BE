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
    public ResponseEntity<List<OrderDTO>> getAll() {
        List<Order> orders = orderService.getAll();
        List<OrderDTO> orderDTOs = orders.stream()
            .map(this::convertToDTO)  // Sử dụng helper method
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        OrderDTO dto = convertToDTO(order);  // Sử dụng helper method
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