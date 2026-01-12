package com.nghiashop.ecome_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nghiashop.ecome_backend.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
