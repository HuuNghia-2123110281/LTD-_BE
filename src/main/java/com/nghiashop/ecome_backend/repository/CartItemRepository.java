package com.nghiashop.ecome_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nghiashop.ecome_backend.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
