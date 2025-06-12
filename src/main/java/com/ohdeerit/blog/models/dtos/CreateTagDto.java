package com.ohdeerit.blog.models.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTagDto(
        @NotNull(message = "Tag name is required")
        @Size(min = 2, max = 30, message = "Tag name must be between {min} and {max}")
        String name
) {
}
