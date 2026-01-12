package com.nghiashop.ecome_backend.service;

import java.util.List;

import com.nghiashop.ecome_backend.entity.Category;

public interface CategoryService {

    List<Category> getAll();

    Category getById(Long id);

    Category create(Category category);

    Category update(Long id, Category category);

    void delete(Long id);
}
