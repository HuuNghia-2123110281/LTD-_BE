package com.nghiashop.ecome_backend.dto.Response;

import java.util.List;

import com.nghiashop.ecome_backend.entity.CartItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long cartId;
    private List<CartItem> items;
    private Integer totalItems;
    private Long totalPrice;
}
