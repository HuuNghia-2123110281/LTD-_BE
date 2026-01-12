package com.nghiashop.ecome_backend.service;

import java.util.List;

import com.nghiashop.ecome_backend.entity.Cart;

public interface CartService {
    List<Cart> getAll();

    Cart getById(Long id);

    Cart create(Cart cart);

    Cart update(Long id, Cart cart);

    void delete(Long id);
}
