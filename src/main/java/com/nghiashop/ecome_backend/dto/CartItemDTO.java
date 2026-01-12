package com.nghiashop.ecome_backend.dto;

import com.nghiashop.ecome_backend.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Product product;
    private int quantity;
    private double price;
}