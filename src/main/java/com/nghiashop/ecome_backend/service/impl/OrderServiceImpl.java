package com.nghiashop.ecome_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nghiashop.ecome_backend.entity.Order;
import com.nghiashop.ecome_backend.entity.User;
import com.nghiashop.ecome_backend.repository.OrderRepository;
import com.nghiashop.ecome_backend.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    // ✅ Method mới
    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Order getById(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }

    @Override
    public Order create(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order update(Long id, Order order) {
        Order existing = getById(id);
        existing.setTotalPrice(order.getTotalPrice());
        existing.setStatus(order.getStatus());
        existing.setUser(order.getUser());
        return orderRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        orderRepository.deleteById(id);
    }
}