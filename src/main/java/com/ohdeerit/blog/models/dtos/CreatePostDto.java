package com.ohdeerit.blog.models.dtos;

import org.springframework.web.multipart.MultipartFile;
import com.ohdeerit.blog.models.enums.PostStatus;
import jakarta.validation.constraints.*;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CreatePostDto(
        @NotBlank(message = "Post title is required")
        @Size(
                min = 10,
                max = 255,
                message = "Post title must be between {min} and {max} characters"
        )
        String title,

        @NotBlank(message = "Post short description is required")
        @Size(
                min = 10,
                max = 1000,
                message = "Post short description must be between {min} and {max} characters"
        )
        String shortDescription,

        @NotBlank(message = "Post content is required")
        @Size(min = 10, message = "Post content must be at least {min} characters")
        String content,

        @NotNull(message = "Category is required")
        UUID categoryId,

        @Size(max = 4, message = "You can only select up to {max} tags")
        Set<UUID> tagIds,

        @NotNull(message = "Post status is required")
        PostStatus status,

        @Positive(message = "Media ID must be a positive number")
        Integer mediaId,

        @Valid
        @NotNull(message = "Thumbnail is required")
        MultipartFile thumbnailFile,

        @Nullable
        @Size(min = 1, max = 10, message = "You must upload between 1 and 10 files")
        MultipartFile[] files,

        @Nullable
        @Size(min = 1, message = "At least one file URL is required")
        List<String> fileUrls
) {
}
