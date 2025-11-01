package com.ohdeerit.blog.models.dtos;

import com.ohdeerit.blog.models.enums.ThumbnailMethod;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ThumbnailDto(
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
