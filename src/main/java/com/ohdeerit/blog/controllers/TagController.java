package com.ohdeerit.blog.controllers;

import com.ohdeerit.blog.domain.dtos.CreateTagsRequest;
import com.ohdeerit.blog.domain.entities.TagEntity;
import com.ohdeerit.blog.domain.dtos.TagResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.ohdeerit.blog.services.TagService;
import com.ohdeerit.blog.mappers.TagMapper;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final TagMapper tagMapper;

    @GetMapping
    public ResponseEntity<List<TagResponse>> getTags() {

        List<TagEntity> tagEntities = tagService.getTags();

        List<TagResponse> tags = tagEntities.stream().map(tagMapper::map).toList();

        return ResponseEntity.ok(tags);
    }

    @PostMapping
    public ResponseEntity<List<TagResponse>> createTags(@RequestBody @Valid CreateTagsRequest createTagsRequest) {
        List<TagEntity> savedTags = tagService.createTags(createTagsRequest.getNames());

        List<TagResponse> tags = savedTags.stream()
                .map(tagMapper::map).toList();

        return new ResponseEntity<>(tags, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
