package com.ohdeerit.blog.models.dtos;

import java.nio.file.Path;
import java.util.List;

public record ProcessedImagesDto(
        Path mediaDirectory,
        List<ProcessedImageInfoDto> processedImages
) {
}
