package com.nghiashop.ecome_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nghiashop.ecome_backend.entity.Cart;
import com.nghiashop.ecome_backend.entity.User;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
