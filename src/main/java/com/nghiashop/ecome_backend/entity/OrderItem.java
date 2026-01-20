package com.nghiashop.ecome_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với sản phẩm
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    
    // Lưu giá tiền tại thời điểm mua (để sau này giá sản phẩm đổi cũng không ảnh hưởng đơn cũ)
    private Long price; 

    // --- SỬA LỖI TẠI ĐÂY ---
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore 
    private Order order;
}