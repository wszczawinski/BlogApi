package com.ohdeerit.blog.models.dtos;

public record MediaFileDto(
        Integer id,
        Integer mediaId,
        String file,
        String shortDescription,
        Integer size,
        Integer position
) {
}
