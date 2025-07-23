package com.ohdeerit.blog.services.interfaces;

import com.ohdeerit.blog.models.dtos.CreatePostDto;
import org.springframework.data.domain.Pageable;
import com.ohdeerit.blog.models.dtos.PostDto;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface PostService {
    PostDto getPost(UUID id);

    Slice<PostDto> getPosts(UUID categoryId, UUID tagId, Pageable pageable);

    Slice<PostDto> getDraftPosts(UUID userId, Pageable pageable);

    PostDto createPost(CreatePostDto post, UUID userId);

    PostDto getPostBySlug(String slug);
}
