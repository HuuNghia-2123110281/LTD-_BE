package com.nghiashop.ecome_backend.service;

import java.util.List;

import com.nghiashop.ecome_backend.entity.OrderItem;

public interface OrderItemService {
    List<OrderItem> getAll();

    OrderItem getById(Long id);

    OrderItem create(OrderItem orderItem);

    OrderItem update(Long id, OrderItem orderItem);

    void delete(Long id);
}
