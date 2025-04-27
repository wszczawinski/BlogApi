package com.ohdeerit.blog.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category nam must be between {min} and {max} ")
    @Pattern(regexp = "^[\\w\\s-]+$", message = "Category name can only contain letters, numbers, spaces and hyphens")
    private String name;

}
