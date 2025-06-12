package com.ohdeerit.blog.api.request;

import com.ohdeerit.blog.models.dtos.CreatePostDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

public record CreatePostRequest(
        @Valid
        @NotNull
        CreatePostDto post
) {
}
