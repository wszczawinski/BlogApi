package com.ohdeerit.blog.api.request;

import com.ohdeerit.blog.models.dtos.UpdatePostDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

public record UpdatePostRequest(
        @Valid
        @NotNull
        UpdatePostDto post
) {
}
