package com.ohdeerit.blog.models.dtos;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.nio.file.Path;
import java.util.List;

public record SaveImageDto(
    @Valid 
    @NotNull 
    MultipartFile originalFile,

    @NotNull 
    Path uploadDirectory,

    @NotNull
    @NotEmpty
    List<ThumbnailDto> thumbnails
) {
}
