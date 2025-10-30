package com.ohdeerit.blog.models.dtos;

import java.nio.file.Path;
import java.util.List;

public record ProcessedFilesDto(
        Path mediaDirectory,
        List<ProcessedFileInfoDto> processedFiles
) {
}
