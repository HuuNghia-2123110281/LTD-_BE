package com.nghiashop.ecome_backend.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nghiashop.ecome_backend.entity.CartItem;
import com.nghiashop.ecome_backend.repository.CartItemRepository;
import com.nghiashop.ecome_backend.service.CartItemService;

@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public List<CartItem> getAll() {
        return cartItemRepository.findAll();
    }

    @Override
    public CartItem getById(Long id) {
        return cartItemRepository.findById(id).orElseThrow();
    }

    @Override
    public CartItem create(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem update(Long id, CartItem cartItem) {
        CartItem existing = getById(id);
        existing.setQuantity(cartItem.getQuantity());
        existing.setProduct(cartItem.getProduct());
        existing.setCart(cartItem.getCart());
        return cartItemRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        cartItemRepository.deleteById(id);
    }
}
