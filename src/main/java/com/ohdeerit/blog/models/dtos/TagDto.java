package com.ohdeerit.blog.models.dtos;

import java.util.UUID;

public record TagDto(
        UUID id,
        String name,
        Integer postCount
) {
}
