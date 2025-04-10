package com.ohdeerit.blog.controllers;

import com.ohdeerit.blog.models.controllers.v1.category.CreateCategoryRequest;
import com.ohdeerit.blog.services.CategoryService;
import com.ohdeerit.blog.models.dtos.CategoryDto;
import org.springframework.web.bind.annotation.*;
import com.ohdeerit.blog.mappers.CategoryMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/categories")
public class CategoryController {

    private final CategoryMapper categoryMapper;
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories() {

        final List<CategoryDto> categories = categoryService.getCategories();

        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequest categoryRequest) {

        final CategoryDto categoryDto = categoryMapper.map(categoryRequest);

        final CategoryDto savedCategory = categoryService.createCategory(categoryDto);

        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {

        categoryService.deleteCategory(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
