package com.ohdeerit.blog.models.dtos;

import com.ohdeerit.blog.models.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Set;

public record PostDto(
        UUID id,
        String title,
        String content,
        AuthorDto author,
        PostStatus status,
        CategoryDto category,
        Set<TagDto> tags,
        Integer readingTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
