package com.ohdeerit.blog.domain.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTagsRequest {

    @NotEmpty(message = "You must provide at least one tag")
    @Size(max = 10, message = "You can only add up to {max} tags")
    private Set<@Size(min = 2, max = 30, message = "Tag name must be between {min} and {max}")
            String> names;

}
