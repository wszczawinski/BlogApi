package com.ohdeerit.blog.services.impl;

import org.springframework.transaction.annotation.Transactional;
import com.ohdeerit.blog.services.mappers.PostServiceMapper;
import org.springframework.beans.factory.annotation.Value;
import com.ohdeerit.blog.repositories.PostMediaRepository;
import com.ohdeerit.blog.repositories.PostRepository;
import com.ohdeerit.blog.config.ThumbnailConstants;
import jakarta.persistence.EntityNotFoundException;
import com.ohdeerit.blog.models.enums.PostStatus;
import org.springframework.data.domain.Pageable;
import com.ohdeerit.blog.services.interfaces.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Slice;
import com.ohdeerit.blog.models.entities.*;
import jakarta.annotation.PostConstruct;
import com.ohdeerit.blog.models.dtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.UUID;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    @Value("${app.post.thumbnail.upload-dir}")
    private String uploadDirectory;

    @PostConstruct
    private void init() throws IOException {
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("[PostServiceImpl.init] Created upload directory: {}", uploadPath);
        } else {
            log.info("[PostServiceImpl.init] Upload directory already exists: {}", uploadPath);
        }

        Path thumbnailPath = uploadPath.resolve("thumbnail");
        if (!Files.exists(thumbnailPath)) {
            Files.createDirectories(thumbnailPath);
            log.info("[PostServiceImpl.init] Created thumbnail directory: {}", thumbnailPath);
        } else {
            log.info("[PostServiceImpl.init] Thumbnail directory already exists: {}", thumbnailPath);
        }
    }

    private final CategoryService categoryService;
    private final UserService userService;
    private final TagService tagService;
    private final MediaService mediaService;
    private final ImageService imageService;

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
    public PostDto createPost(final CreatePostDto post, final UUID userId) {
        final CategoryEntity categoryEntity = categoryService.getCategory(post.categoryId());
        final UserEntity userEntity = userService.getUser(userId);

        final var saveImageDto = new SaveImageDto(post.thumbnailFile(), Paths.get(uploadDirectory),
                List.of(ThumbnailConstants.POST_THUMBNAIL));
        final String thumbnailFileName = imageService.saveImage(saveImageDto);

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
        newPost.setThumbnail(thumbnailFileName);
        newPost.setShortDescription(post.shortDescription());
        if (tagEntities != null) {
            newPost.setTags(new HashSet<>(tagEntities));
        }

        if (mediaEntity != null) {
            newPost.setMedia(mediaEntity);
        }

        final PostEntity savedPost = postRepository.save(newPost);
        log.info("[PostService.createPost] Post '{}': {}", savedPost.getId(), post.title());

        return postMapper.map(savedPost);
    }

    @Override
    @Transactional
    public PostDto updatePost(final UpdatePostDto updatePost, final UUID userId) {
        PostEntity existingPost = postRepository.findById(updatePost.id())
                .orElseThrow(() -> new EntityNotFoundException("Post with id " + updatePost.id() + " not found"));

        if (updatePost.title() != null) {
            existingPost.setTitle(updatePost.title());
        }

        if (updatePost.shortDescription() != null) {
            existingPost.setShortDescription(updatePost.shortDescription());
        }

        if (updatePost.content() != null) {
            existingPost.setContent(updatePost.content());
            existingPost.setReadingTime(calculateReadTime(updatePost.content()));
        }

        if (updatePost.categoryId() != null) {
            CategoryEntity category = categoryService.getCategory(updatePost.categoryId());
            existingPost.setCategory(category);
        }

        if (updatePost.tagIds() != null) {
            Set<TagEntity> tags = updatePost.tagIds().stream()
                    .map(tagService::getTag)
                    .collect(Collectors.toSet());
            existingPost.getTags().clear();
            existingPost.getTags().addAll(tags);
        }

        if (updatePost.status() != null) {
            existingPost.setStatus(updatePost.status());
        }

        if (updatePost.mediaId() != null) {
            if (existingPost.getMedia() != null && !existingPost.getMedia().getId().equals(updatePost.mediaId())) {
                postMediaRepository.deleteByPostId(existingPost.getId());
                postMediaRepository.flush();
            }

            final MediaEntity mediaEntity = mediaService.getMedia(updatePost.mediaId());
            existingPost.setMedia(mediaEntity);
        }

        if (updatePost.thumbnailFile() != null) {
            final var saveImageDto = new SaveImageDto(updatePost.thumbnailFile(), Paths.get(uploadDirectory),
                    List.of(ThumbnailConstants.POST_THUMBNAIL));
            final String thumbnailFileName = imageService.saveImage(saveImageDto);

            existingPost.setThumbnail(thumbnailFileName);
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
