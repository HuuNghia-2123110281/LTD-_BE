package com.nghiashop.ecome_backend.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nghiashop.ecome_backend.entity.Product;
import com.nghiashop.ecome_backend.repository.ProductRepository;
import com.nghiashop.ecome_backend.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, Product product) {
        Product existing = getById(id);
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        existing.setImage(product.getImage());
        existing.setRating(product.getRating());
        existing.setSold(product.getSold());
        existing.setStock(product.getStock());
        existing.setDescription(product.getDescription());
        existing.setCategory(product.getCategory());
        return productRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> getByCategory(Long categoryId) {
        return productRepository.findByCategory_Id(categoryId);
    }

    @Override
    public List<Product> search(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }
}
