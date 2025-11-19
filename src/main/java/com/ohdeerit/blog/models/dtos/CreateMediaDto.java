package com.ohdeerit.blog.models.dtos;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;

public record CreateMediaDto(
        @NotBlank(message = "Short description is required")
        @Size(max = 500, message = "Short description must not exceed 500 characters")
        String shortDescription,

        @NotNull(message = "Status is required")
        @Min(value = 1, message = "Status must be 1 (visible) or 2 (hidden)")
        @Max(value = 2, message = "Status must be 1 (visible) or 2 (hidden)")
        Byte status,

        @NotNull(message = "Files are required")
        @Size(min = 1, max = 100, message = "You must upload between 1 and 100 files")
        MultipartFile[] files
) {
}
