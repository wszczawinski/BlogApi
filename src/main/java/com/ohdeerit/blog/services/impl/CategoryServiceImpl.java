package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.repositories.CategoryRepository;
import com.ohdeerit.blog.domain.entities.CategoryEntity;
import com.ohdeerit.blog.services.CategoryService;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryEntity> getCategories() {
        return categoryRepository.findAllWithPostCount();
    }

    @Override
    @Transactional
    public CategoryEntity createCategory(CategoryEntity category) {

        final String name = category.getName();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category with name " + name + " already exists");
        }

        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(UUID id) {

        Optional<CategoryEntity> category = categoryRepository.findById(id);

        if (category.isEmpty()) {
            throw new IllegalArgumentException("Category with id " + id + " does not exist");
        }

        if (!category.get().getPosts().isEmpty()) {
            throw new IllegalStateException("Category with id " + id + " has posts");
        }

        categoryRepository.deleteById(id);
    }
}
