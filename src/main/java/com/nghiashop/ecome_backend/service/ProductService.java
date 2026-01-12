package com.nghiashop.ecome_backend.service;

import java.util.List;

import com.nghiashop.ecome_backend.entity.Product;

public interface ProductService {

    List<Product> getAll();

    Product getById(Long id);

    Product create(Product product);

    Product update(Long id, Product product);

    void delete(Long id);

    List<Product> getByCategory(Long categoryId);

    List<Product> search(String keyword);
}
