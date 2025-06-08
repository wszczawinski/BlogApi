package com.ohdeerit.blog.controllers;

import org.springframework.validation.annotation.Validated;
import com.ohdeerit.blog.domain.dtos.CreatePostRequest;
import com.ohdeerit.blog.domain.entities.PostEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.NotNull;
import com.ohdeerit.blog.services.PostService;
import com.ohdeerit.blog.domain.dtos.PostDto;
import com.ohdeerit.blog.mappers.PostMapper;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping(path = "/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private final PostMapper postMapper;

    @GetMapping
    public ResponseEntity<List<PostDto>> getPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId
    ) {
        List<PostEntity> postEntities = postService.getPosts(categoryId, tagId);
        List<PostDto> posts = postEntities.stream().map(postMapper::map).toList();

        return ResponseEntity.ok(posts);
    }

    @GetMapping(path = "/drafts")
    public ResponseEntity<List<PostDto>> getDrafts(
            @RequestAttribute UUID userId
    ) {
        List<PostEntity> postEntities = postService.getDraftPosts(userId);
        List<PostDto> posts = postEntities.stream().map(postMapper::map).toList();

        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @RequestBody @NotNull CreatePostRequest createPostRequest,
            @RequestAttribute UUID userId
    ) {
        final PostEntity postEntity = postService.createPost(createPostRequest.post(), userId);
        final PostDto postDto = postMapper.map(postEntity);

        return new ResponseEntity<>(postDto, HttpStatus.CREATED);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable UUID id) {
        final PostEntity postEntity = postService.getPost(id);
        final PostDto postDto = postMapper.map(postEntity);

        return ResponseEntity.ok(postDto);
    }
}
