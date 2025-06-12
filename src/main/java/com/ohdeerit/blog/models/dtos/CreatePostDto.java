package com.ohdeerit.blog.models.dtos;

import com.ohdeerit.blog.models.enums.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CreatePostDto(
        @Size(min = 10, max = 200, message = "Post title must be between {min} and {max} characters")
        String title,

        @NotBlank(message = "Post content is required")
        @Size(min = 10, message = "Post content must be at least {min} characters")
        String content,

        @NotNull(message = "Category is required")
        UUID categoryId,

        @Size(max = 4, message = "You can only select up to {max} tags")
        Set<UUID> tagIds,

        @NotNull(message = "Post status is required")
        PostStatus status
) {
}
