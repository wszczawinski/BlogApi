package com.ohdeerit.blog.models.dtos;

import java.util.UUID;

public record AuthorDto(
        UUID id,
        String name
) {
}
