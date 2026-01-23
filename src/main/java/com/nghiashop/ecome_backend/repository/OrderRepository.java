package com.nghiashop.ecome_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nghiashop.ecome_backend.entity.Order;
import com.nghiashop.ecome_backend.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
