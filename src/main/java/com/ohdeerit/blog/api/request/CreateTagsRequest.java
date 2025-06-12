package com.ohdeerit.blog.api.request;

import com.ohdeerit.blog.models.dtos.CreateTagDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.util.Set;

public record CreateTagsRequest(
        @Valid
        @NotEmpty(message = "You must provide at least one tag")
        @Size(max = 10, message = "You can only add up to {max} tags")
        Set<CreateTagDto> tags
) {
}
