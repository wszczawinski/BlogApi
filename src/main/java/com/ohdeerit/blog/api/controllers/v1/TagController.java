package com.ohdeerit.blog.api.controllers.v1;

import com.ohdeerit.blog.api.request.CreateTagsRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.ohdeerit.blog.services.interfaces.TagService;
import com.ohdeerit.blog.models.dtos.TagDto;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping(path = "/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDto>> getTags() {
        List<TagDto> tags = tagService.getTags();

        return ResponseEntity.ok(tags);
    }

    @PostMapping
    public ResponseEntity<List<TagDto>> createTags(@RequestBody @Valid CreateTagsRequest createTagsRequest) {
        List<TagDto> tags  = tagService.createTags(createTagsRequest.tags());

        return new ResponseEntity<>(tags, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
