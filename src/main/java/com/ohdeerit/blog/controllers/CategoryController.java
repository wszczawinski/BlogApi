package com.ohdeerit.blog.controllers;

import com.ohdeerit.blog.domain.dtos.CreateCategoryRequest;
import com.ohdeerit.blog.domain.entities.CategoryEntity;
import com.ohdeerit.blog.services.CategoryService;
import com.ohdeerit.blog.domain.dtos.CategoryDto;
import org.springframework.web.bind.annotation.*;
import com.ohdeerit.blog.mappers.CategoryMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories() {

        final List<CategoryDto> categories = categoryService.getCategories().stream()
                .map(categoryMapper::map)
                .toList();

        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody @Valid CreateCategoryRequest categoryRequest) {

        final CategoryEntity category = categoryMapper.map(categoryRequest);

        final CategoryEntity createdCategory = categoryService.createCategory(category);

        return new ResponseEntity<>(categoryMapper.map(createdCategory), HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {

        categoryService.deleteCategory(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
