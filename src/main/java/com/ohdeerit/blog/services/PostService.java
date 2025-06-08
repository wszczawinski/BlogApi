package com.ohdeerit.blog.services;

import com.ohdeerit.blog.domain.entities.PostEntity;
import com.ohdeerit.blog.domain.dtos.CreatePostDto;

import java.util.List;
import java.util.UUID;

public interface PostService {

    PostEntity getPost(UUID id);
    List<PostEntity> getPosts(UUID categoryId, UUID tagId);
    List<PostEntity> getDraftPosts(UUID userId);
    PostEntity createPost(CreatePostDto post, UUID userId);

}
