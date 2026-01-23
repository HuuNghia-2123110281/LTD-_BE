package com.nghiashop.ecome_backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long totalPrice;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
}