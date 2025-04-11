package com.ohdeerit.blog.services;

import com.ohdeerit.blog.repositories.CategoryRepository;
import com.ohdeerit.blog.models.entities.CategoryEntity;
import com.ohdeerit.blog.models.dtos.CategoryDto;
import com.ohdeerit.blog.mappers.CategoryMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getCategories() {
        final List<CategoryEntity> categories = categoryRepository.findAllWithPostCount();

        return categoryMapper.map(categories);
    }

    public CategoryDto createCategory(CategoryDto categoryDto) {
        final CategoryEntity categoryEntity = categoryMapper.map(categoryDto);
        final String name = categoryEntity.getName();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category with name " + name + " already exists");
        }

        final CategoryEntity savedCategoryEntity = categoryRepository.save(categoryEntity);

        return categoryMapper.map(savedCategoryEntity);
    }

    public void deleteCategory(UUID id) {
        Optional<CategoryEntity> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            if (!category.get().getPosts().isEmpty()) {
                throw new IllegalStateException("Category has posts and cannot be deleted");
            }
            categoryRepository.deleteById(id);
        }
    }

}
