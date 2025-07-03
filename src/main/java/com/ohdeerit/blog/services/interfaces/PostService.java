package com.ohdeerit.blog.services.interfaces;

import com.ohdeerit.blog.models.dtos.CreatePostDto;
import com.ohdeerit.blog.models.dtos.PostDto;

import java.util.List;
import java.util.UUID;

public interface PostService {
    PostDto getPost(UUID id);

    List<PostDto> getPosts(UUID categoryId, UUID tagId);

    List<PostDto> getDraftPosts(UUID userId);

    PostDto createPost(CreatePostDto post, UUID userId);

    PostDto getPostBySlug(String slug);
}
