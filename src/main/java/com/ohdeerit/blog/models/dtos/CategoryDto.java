package com.ohdeerit.blog.models.dtos;

import java.util.UUID;

public record CategoryDto(
        UUID id,
        String name,
        Long postCount
) {
}
