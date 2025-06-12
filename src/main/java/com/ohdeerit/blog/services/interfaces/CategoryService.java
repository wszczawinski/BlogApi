package com.ohdeerit.blog.services.interfaces;

import com.ohdeerit.blog.models.entities.CategoryEntity;
import com.ohdeerit.blog.models.dtos.CreateCategoryDto;
import com.ohdeerit.blog.models.dtos.CategoryDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryDto> getCategories();

    CategoryDto createCategory(CreateCategoryDto category);

    void deleteCategory(UUID id);

    CategoryEntity getCategory(UUID id);
}
