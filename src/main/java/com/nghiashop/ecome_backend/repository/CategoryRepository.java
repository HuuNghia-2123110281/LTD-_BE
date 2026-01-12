package com.nghiashop.ecome_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nghiashop.ecome_backend.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
