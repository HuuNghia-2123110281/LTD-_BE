package com.nghiashop.ecome_backend.service;

import java.util.List;

import com.nghiashop.ecome_backend.entity.Order;

public interface OrderService {
    List<Order> getAll();

    Order getById(Long id);

    Order create(Order order);

    Order update(Long id, Order order);

    void delete(Long id);
}
