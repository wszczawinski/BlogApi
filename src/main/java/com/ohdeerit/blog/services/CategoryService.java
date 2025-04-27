package com.ohdeerit.blog.services;

import com.ohdeerit.blog.domain.entities.CategoryEntity;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryEntity> getCategories();

    CategoryEntity createCategory(CategoryEntity category);

    void deleteCategory(UUID id);
}
