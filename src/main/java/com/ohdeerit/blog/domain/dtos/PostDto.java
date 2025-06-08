package com.ohdeerit.blog.domain.dtos;

import com.ohdeerit.blog.domain.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Set;

public record PostDto(
        UUID id,
        String title,
        String content,
        AuthorDto author,
        CategoryDto category,
        Set<TagResponse> tags,
        Integer readingTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        PostStatus status
) {
}
