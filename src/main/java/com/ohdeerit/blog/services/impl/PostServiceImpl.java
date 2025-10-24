package com.ohdeerit.blog.services.impl;

import org.springframework.transaction.annotation.Transactional;
import com.ohdeerit.blog.services.mappers.PostServiceMapper;
import com.ohdeerit.blog.repositories.PostMediaRepository;
import com.ohdeerit.blog.repositories.PostRepository;
import com.ohdeerit.blog.models.dtos.UpdatePostDto;
import com.ohdeerit.blog.models.dtos.CreatePostDto;
import jakarta.persistence.EntityNotFoundException;
import com.ohdeerit.blog.models.enums.PostStatus;
import org.springframework.data.domain.Pageable;
import com.ohdeerit.blog.services.interfaces.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Slice;
import com.ohdeerit.blog.models.dtos.PostDto;
import com.ohdeerit.blog.models.entities.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.UUID;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final CategoryService categoryService;
    private final UserService userService;
    private final TagService tagService;
    private final MediaService mediaService;
    private final ThumbnailService thumbnailService;

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;

    private final PostServiceMapper postMapper;

    @Override
    public PostDto getPost(UUID id) {
        final PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No post found with id: " + id));

        return postMapper.map(postEntity);
    }

    @Override
    public PostDto getPostBySlug(String slug) {
        final PostEntity postEntity = postRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with slug: " + slug));

        return postMapper.map(postEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PostDto> getPosts(UUID categoryId, UUID tagId, Pageable pageable) {
        Slice<PostEntity> postEntities;

        if (categoryId != null && tagId != null) {
            CategoryEntity category = categoryService.getCategory(categoryId);
            TagEntity tag = tagService.getTag(tagId);
            postEntities = postRepository.findAllByStatusAndCategoryAndTagsContaining(
                    PostStatus.PUBLISHED, category, tag, pageable);
        } else if (categoryId != null) {
            CategoryEntity category = categoryService.getCategory(categoryId);
            postEntities = postRepository.findAllByStatusAndCategory(
                    PostStatus.PUBLISHED, category, pageable);
        } else if (tagId != null) {
            TagEntity tag = tagService.getTag(tagId);
            postEntities = postRepository.findAllByStatusAndTagsContaining(
                    PostStatus.PUBLISHED, tag, pageable);
        } else {
            postEntities = postRepository.findAllByStatus(PostStatus.PUBLISHED, pageable);
        }

        return postEntities.map(postMapper::map);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PostDto> getAllPosts(Pageable pageable) {
        Slice<PostEntity> posts = postRepository.findAll(pageable);

        return posts.map(postMapper::map);
    }

    @Override
    @Transactional
    public PostDto createPost(CreatePostDto post, UUID userId) {
        final CategoryEntity categoryEntity = categoryService.getCategory(post.categoryId());
        final UserEntity userEntity = userService.getUser(userId);

        final String thumbnailFileName = thumbnailService.create(post.thumbnailFile());
        log.info("Created thumbnail for post '{}': {}", post.title(), thumbnailFileName);
        
        List<TagEntity> tagEntities = null;
        if (post.tagIds() != null && !post.tagIds().isEmpty()) {
            tagEntities = tagService.getTags(post.tagIds());
        }

        MediaEntity mediaEntity = null;
        if (post.mediaId() != null) {
            mediaEntity = mediaService.getMedia(post.mediaId());
        }

        PostEntity newPost = new PostEntity();
        newPost.setCategory(categoryEntity);
        newPost.setAuthor(userEntity);
        newPost.setTitle(post.title());
        newPost.setContent(post.content());
        newPost.setStatus(post.status());
        newPost.setReadingTime(calculateReadTime(post.content()));
        if (tagEntities != null) {
            newPost.setTags(new HashSet<>(tagEntities));
        }

        if (mediaEntity != null) {
            newPost.setMedia(mediaEntity);
        }

        final PostEntity savedPost = postRepository.save(newPost);

        return postMapper.map(savedPost);
    }

    @Override
    @Transactional
    public PostDto updatePost(UUID id, UpdatePostDto updatePostDto, UUID userId) {
        PostEntity existingPost = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post with id " + id + " not found"));

        if (updatePostDto.title() != null) {
            existingPost.setTitle(updatePostDto.title());
        }

        if (updatePostDto.content() != null) {
            existingPost.setContent(updatePostDto.content());
            existingPost.setReadingTime(calculateReadTime(updatePostDto.content()));
        }

        if (updatePostDto.categoryId() != null) {
            CategoryEntity category = categoryService.getCategory(updatePostDto.categoryId());
            existingPost.setCategory(category);
        }

        if (updatePostDto.tagIds() != null) {
            Set<TagEntity> tags = updatePostDto.tagIds().stream()
                    .map(tagService::getTag)
                    .collect(Collectors.toSet());
            existingPost.getTags().clear();
            existingPost.getTags().addAll(tags);
        }

        if (updatePostDto.status() != null) {
            existingPost.setStatus(updatePostDto.status());
        }

        if (updatePostDto.mediaId() != null) {
            if (existingPost.getMedia() != null && !existingPost.getMedia().getId().equals(updatePostDto.mediaId())) {
                postMediaRepository.deleteByPostId(existingPost.getId());
                postMediaRepository.flush();
            }

            final MediaEntity mediaEntity = mediaService.getMedia(updatePostDto.mediaId());
            existingPost.setMedia(mediaEntity);
        }

        final PostEntity updatedPost = postRepository.save(existingPost);
        return postMapper.map(updatedPost);
    }

    private static Integer calculateReadTime(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        int words = content.split("\\s+").length;

        return (int) Math.ceil((double) words / 200);
    }
}
