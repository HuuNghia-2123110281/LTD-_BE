package com.nghiashop.ecome_backend.dto.Response;

import java.util.List;
import com.nghiashop.ecome_backend.dto.CartItemDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long cartId;
    private List<CartItemDTO> items; 
    private int totalItems;
    private double totalPrice;
}