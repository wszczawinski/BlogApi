package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.services.mappers.CategoryServiceMapper;
import com.ohdeerit.blog.services.interfaces.CategoryService;
import com.ohdeerit.blog.repositories.CategoryRepository;
import com.ohdeerit.blog.models.entities.CategoryEntity;
import com.ohdeerit.blog.models.dtos.CreateCategoryDto;
import jakarta.persistence.EntityNotFoundException;
import com.ohdeerit.blog.models.dtos.CategoryDto;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryServiceMapper categoryMapper;

    @Override
    public List<CategoryDto> getCategories() {
        List<CategoryEntity> categoryEntities = categoryRepository.findAllWithPostCount();

        return categoryEntities.stream().map(categoryMapper::map).toList();
    }

    @Override
    public CategoryEntity getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with id " + id + " does not exist"));
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CreateCategoryDto categoryRequest) {
        final CategoryEntity category = categoryMapper.map(categoryRequest);
        final String name = category.getName();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category with name " + name + " already exists");
        }

        final CategoryEntity createdCategory = categoryRepository.save(category);
        
        log.info("[CategoryServiceImpl.createCategory] Created category: {}", createdCategory.getName());

        return categoryMapper.map(createdCategory);
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
        log.info("[CategoryServiceImpl.deleteCategory] Deleted category with id: {}", id);
    }
}
