package com.ohdeerit.blog.api.controllers.v1;

import org.springframework.validation.annotation.Validated;
import com.ohdeerit.blog.services.interfaces.PostService;
import com.ohdeerit.blog.api.request.CreatePostRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.NotNull;
import com.ohdeerit.blog.models.dtos.PostDto;
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

    @GetMapping
    public ResponseEntity<List<PostDto>> getPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId
    ) {
        List<PostDto> posts = postService.getPosts(categoryId, tagId);

        return ResponseEntity.ok(posts);
    }

    @GetMapping(path = "/drafts")
    public ResponseEntity<List<PostDto>> getDrafts(
            @RequestAttribute UUID userId
    ) {
        List<PostDto> posts = postService.getDraftPosts(userId);

        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @RequestBody @NotNull CreatePostRequest createPostRequest,
            @RequestAttribute UUID userId
    ) {
        final PostDto postDto = postService.createPost(createPostRequest.post(), userId);

        return new ResponseEntity<>(postDto, HttpStatus.CREATED);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable UUID id) {
        final PostDto postDto = postService.getPost(id);

        return ResponseEntity.ok(postDto);
    }

    @GetMapping(path = "/slug/{slug}")
    public ResponseEntity<PostDto> getPostBySlug(@PathVariable String slug) {
        final PostDto postDto = postService.getPostBySlug(slug);

        return ResponseEntity.ok(postDto);
    }

}
