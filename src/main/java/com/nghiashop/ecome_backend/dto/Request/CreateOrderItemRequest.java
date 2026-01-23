package com.nghiashop.ecome_backend.dto.Request;
import lombok.Data;

@Data
public class CreateOrderItemRequest {
    private Long productId;
    private int quantity;
    private Long price;
}