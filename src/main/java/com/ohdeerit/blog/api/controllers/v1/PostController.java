package com.ohdeerit.blog.api.controllers.v1;

import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Value;
import com.ohdeerit.blog.services.interfaces.PostService;
import com.ohdeerit.blog.api.request.CreatePostRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Slice;
import com.ohdeerit.blog.models.dtos.PostDto;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Validated
@RestController
@RequestMapping(path = "/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Value("${app.pagination.posts-per-page}")
    private int postsPerPage;

    @GetMapping
    public ResponseEntity<Slice<PostDto>> getPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId,
            @RequestParam(defaultValue = "1") int page
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, postsPerPage, sort);
        Slice<PostDto> posts = postService.getPosts(categoryId, tagId, pageable);

        return ResponseEntity.ok(posts);
    }

    @GetMapping(path = "/drafts")
    public ResponseEntity<Slice<PostDto>> getDrafts(
            @RequestAttribute UUID userId,
            @RequestParam(defaultValue = "1") int page
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, postsPerPage, sort);
        Slice<PostDto> posts = postService.getDraftPosts(userId, pageable);

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
