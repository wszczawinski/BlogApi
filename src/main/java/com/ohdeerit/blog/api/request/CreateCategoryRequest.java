package com.ohdeerit.blog.api.request;

import com.ohdeerit.blog.models.dtos.CreateCategoryDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

public record CreateCategoryRequest(
        @Valid
        @NotNull
        CreateCategoryDto category
) {
}
