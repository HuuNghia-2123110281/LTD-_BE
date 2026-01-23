package com.nghiashop.ecome_backend.service;

import java.util.List;
import com.nghiashop.ecome_backend.entity.Order;
import com.nghiashop.ecome_backend.entity.User;

public interface OrderService {
    List<Order> getAll();
    
    List<Order> getOrdersByUser(User user);
    
    Order getById(Long id);
    
    Order create(Order order);
    
    Order update(Long id, Order order);
    
    void delete(Long id);
}