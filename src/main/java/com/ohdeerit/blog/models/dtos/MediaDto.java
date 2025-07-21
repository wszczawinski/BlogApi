package com.ohdeerit.blog.models.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record MediaDto(
        Integer id,
        String shortDescription,
        String shortSlug,
        String folder,
        byte type,
        byte status,
        LocalDateTime updatedAt,
        List<MediaFileDto> mediaFiles
) {
}
