package com.ohdeerit.blog.services.interfaces;

import com.ohdeerit.blog.models.dtos.SaveImageDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

public interface ImageService {
    String saveImage(@Valid @NotNull final SaveImageDto saveImageDto);
}
