package com.ohdeerit.blog.api.request;

import com.ohdeerit.blog.models.dtos.CreateMediaDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

public record CreateMediaRequest(
        @Valid
        @NotNull
        CreateMediaDto media
) {
}
