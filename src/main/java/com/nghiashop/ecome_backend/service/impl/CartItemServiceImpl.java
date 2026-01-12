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
        return cartItemRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng")
        );
    }

    @Override
    public CartItem create(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem update(Long id, CartItem cartItem) {
        CartItem existing = getById(id);
        existing.setQuantity(cartItem.getQuantity());
        existing.setPrice(cartItem.getPrice());
        existing.setProduct(cartItem.getProduct());
        existing.setCart(cartItem.getCart());
        return cartItemRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        cartItemRepository.deleteById(id);
    }

    @Override
    public CartItem findByCartIdAndProductId(Long cartId, Long productId) {
        return cartItemRepository.findByCartIdAndProductId(cartId, productId)
                .orElse(null);
    }

    @Override
    public List<CartItem> findByCartId(Long cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    @Override
    public void deleteByCartId(Long cartId) {
        List<CartItem> items = findByCartId(cartId);
        cartItemRepository.deleteAll(items);
    }
}