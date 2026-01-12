package com.nghiashop.ecome_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nghiashop.ecome_backend.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory_Id(Long categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);
}
