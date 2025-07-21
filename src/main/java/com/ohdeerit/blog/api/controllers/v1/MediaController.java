package com.ohdeerit.blog.api.controllers.v1;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.annotation.Validated;
import com.ohdeerit.blog.services.interfaces.MediaService;
import com.ohdeerit.blog.api.request.CreateMediaRequest;
import org.springframework.http.ResponseEntity;
import com.ohdeerit.blog.models.dtos.MediaDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping(path = "/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaDto> createMedia(@ModelAttribute @Valid CreateMediaRequest request) {
        MediaDto createdMedia = mediaService.createMedia(request.media());

        return new ResponseEntity<>(createdMedia, HttpStatus.CREATED);
    }
}
