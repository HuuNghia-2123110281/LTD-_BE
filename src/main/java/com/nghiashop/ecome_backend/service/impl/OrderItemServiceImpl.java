package com.nghiashop.ecome_backend.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nghiashop.ecome_backend.entity.OrderItem;
import com.nghiashop.ecome_backend.repository.OrderItemRepository;
import com.nghiashop.ecome_backend.service.OrderItemService;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public List<OrderItem> getAll() {
        return orderItemRepository.findAll();
    }

    @Override
    public OrderItem getById(Long id) {
        return orderItemRepository.findById(id).orElseThrow();
    }

    @Override
    public OrderItem create(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public OrderItem update(Long id, OrderItem orderItem) {
        OrderItem existing = getById(id);
        existing.setQuantity(orderItem.getQuantity());
        existing.setPrice(orderItem.getPrice());
        existing.setProduct(orderItem.getProduct());
        existing.setOrder(orderItem.getOrder());
        return orderItemRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        orderItemRepository.deleteById(id);
    }
}
