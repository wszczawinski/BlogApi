package com.ohdeerit.blog.models.dtos;

import org.springframework.web.multipart.MultipartFile;
import com.ohdeerit.blog.models.enums.ThumbnailMethod;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.nio.file.Path;

public record SaveImageDto(
    @Valid 
    @NotNull 
    MultipartFile originalFile,

    @NotNull 
    Path uploadDirectory,
    
    @NotNull 
    @Positive int width,

    @NotNull 
    @Positive int height,

    @NotNull 
    ThumbnailMethod method,

    @NotNull 
    @Min(1) 
    @Max(100) 
    int percent
) {
}
