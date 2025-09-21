package com.ohdeerit.blog.api.controllers.v1;

import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Value;
import com.ohdeerit.blog.services.interfaces.PostService;
import com.ohdeerit.blog.api.request.UpdatePostRequest;
import com.ohdeerit.blog.api.request.CreatePostRequest;
import com.ohdeerit.blog.services.mappers.SliceMapper;
import com.ohdeerit.blog.api.response.SliceResponse;
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
import jakarta.validation.Valid;

import java.util.UUID;

@Validated
@RestController
@RequestMapping(path = "/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final SliceMapper sliceMapper;

    @Value("${app.pagination.posts-per-page}")
    private int postsPerPage;

    @GetMapping
    public ResponseEntity<SliceResponse<PostDto>> getPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId,
            @RequestParam(defaultValue = "1") int page
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, postsPerPage, sort);
        Slice<PostDto> posts = postService.getPosts(categoryId, tagId, pageable);
        SliceResponse<PostDto> response = sliceMapper.toSliceResponse(posts);

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<SliceResponse<PostDto>> getAllPosts(
            @RequestAttribute UUID userId,
            @RequestParam(defaultValue = "1") int page
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, postsPerPage, sort);
        Slice<PostDto> posts = postService.getAllPosts(pageable);
        SliceResponse<PostDto> response = sliceMapper.toSliceResponse(posts);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @RequestBody @NotNull CreatePostRequest createPostRequest,
            @RequestAttribute UUID userId
    ) {
        final PostDto postDto = postService.createPost(createPostRequest.post(), userId);

        return new ResponseEntity<>(postDto, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable UUID id,
            @RequestBody @NotNull @Valid UpdatePostRequest updatePostRequest,
            @RequestAttribute UUID userId
    ) {
        final PostDto postDto = postService.updatePost(id, updatePostRequest.post(), userId);

        return ResponseEntity.ok(postDto);
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
