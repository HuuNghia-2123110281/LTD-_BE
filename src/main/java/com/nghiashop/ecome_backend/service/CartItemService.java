package com.nghiashop.ecome_backend.service;

import java.util.List;
import com.nghiashop.ecome_backend.entity.CartItem;

public interface CartItemService {
    List<CartItem> getAll();
    CartItem getById(Long id);
    CartItem create(CartItem cartItem);
    CartItem update(Long id, CartItem cartItem);
    void delete(Long id);
    CartItem findByCartIdAndProductId(Long cartId, Long productId);
    List<CartItem> findByCartId(Long cartId);
    void deleteByCartId(Long cartId);
}