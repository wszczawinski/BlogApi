package com.ohdeerit.blog.models.dtos;

import com.ohdeerit.blog.models.enums.PostStatus;
import jakarta.validation.constraints.*;

import java.util.UUID;
import java.util.Set;

public record UpdatePostDto(
        @Size(min = 10, max = 200, message = "Post title must be between {min} and {max} characters")
        String title,

        @Size(min = 10, message = "Post content must be at least {min} characters")
        String content,

        UUID categoryId,

        @Size(max = 4, message = "You can only select up to {max} tags")
        Set<UUID> tagIds,

        PostStatus status,

        @Positive(message = "Media ID must be a positive number")
        Integer mediaId
) {
}
