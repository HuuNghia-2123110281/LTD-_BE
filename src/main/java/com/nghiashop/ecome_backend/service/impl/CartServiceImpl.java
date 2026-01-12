package com.nghiashop.ecome_backend.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nghiashop.ecome_backend.entity.Cart;
import com.nghiashop.ecome_backend.repository.CartRepository;
import com.nghiashop.ecome_backend.service.CartService;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Override
    public List<Cart> getAll() {
        return cartRepository.findAll();
    }

    @Override
    public Cart getById(Long id) {
        return cartRepository.findById(id).orElseThrow();
    }

    @Override
    public Cart create(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public Cart update(Long id, Cart cart) {
        Cart existing = getById(id);
        existing.setUser(cart.getUser());
        return cartRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        cartRepository.deleteById(id);
    }
}
