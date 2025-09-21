package com.ohdeerit.blog.api.controllers.v1;

import org.springframework.validation.annotation.Validated;
import com.ohdeerit.blog.services.interfaces.MediaService;
import org.springframework.beans.factory.annotation.Value;
import com.ohdeerit.blog.api.request.CreateMediaRequest;
import com.ohdeerit.blog.services.mappers.SliceMapper;
import com.ohdeerit.blog.api.response.SliceResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import com.ohdeerit.blog.models.dtos.MediaDto;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
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
    private final SliceMapper sliceMapper;

    @Value("${app.pagination.posts-per-page}")
    private int mediaPerPage;

    @GetMapping
    public ResponseEntity<SliceResponse<MediaDto>> getMedia(
            @RequestParam(defaultValue = "1") int page
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page - 1, mediaPerPage, sort);

        Slice<MediaDto> media = mediaService.getMedia(pageable);
        SliceResponse<MediaDto> response = sliceMapper.toSliceResponse(media);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaDto> createMedia(@ModelAttribute @Valid CreateMediaRequest request) {
        MediaDto createdMedia = mediaService.createMedia(request.media());

        return new ResponseEntity<>(createdMedia, HttpStatus.CREATED);
    }
}
