package com.nghiashop.ecome_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nghiashop.ecome_backend.dto.OrderDTO;
import com.nghiashop.ecome_backend.dto.OrderItemDTO;
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

    // Thêm phương thức mới để chuyển đổi Order -> OrderDTO
    public OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());

        // Chuyển đổi OrderItems
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
            .map(item -> {
                OrderItemDTO itemDTO = new OrderItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setProductId(item.getProduct().getId());
                itemDTO.setProductName(item.getProduct().getName());
                itemDTO.setProductImage(item.getProduct().getImage());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setPrice(item.getPrice());
                return itemDTO;
            })
            .collect(Collectors.toList());

        dto.setItems(itemDTOs);
        return dto;
    }

    // Lấy tất cả đơn hàng của user và chuyển sang DTO
    public List<OrderDTO> getAllByUser(User user) {
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Lấy chi tiết 1 đơn hàng và chuyển sang DTO
    public OrderDTO getDTOById(Long id) {
        Order order = getById(id);
        return convertToDTO(order);
    }
}