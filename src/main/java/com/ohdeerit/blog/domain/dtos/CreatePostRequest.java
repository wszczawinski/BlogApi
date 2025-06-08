package com.ohdeerit.blog.domain.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

public record CreatePostRequest(
        @Valid @NotNull CreatePostDto post
) {
}
