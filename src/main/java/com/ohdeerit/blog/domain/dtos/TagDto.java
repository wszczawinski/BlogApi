package com.ohdeerit.blog.domain.dtos;

import java.util.UUID;

public record TagDto(

        UUID id,
        String name,
        Integer postCount

) {
}
