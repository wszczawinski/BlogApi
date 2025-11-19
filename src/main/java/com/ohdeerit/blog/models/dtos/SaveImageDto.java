package com.ohdeerit.blog.models.dtos;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

import java.nio.file.Path;
import java.util.List;

public record SaveImageDto(
    @Valid 
    @NotNull 
    MultipartFile originalFile,

    @NotNull 
    Path uploadDirectory,

    @Nullable
    List<ThumbnailDto> thumbnails
) {
}
