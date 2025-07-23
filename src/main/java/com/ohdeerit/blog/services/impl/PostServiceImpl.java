package com.ohdeerit.blog.services.impl;

import org.springframework.transaction.annotation.Transactional;
import com.ohdeerit.blog.services.interfaces.CategoryService;
import com.ohdeerit.blog.services.mappers.PostServiceMapper;
import com.ohdeerit.blog.services.interfaces.UserService;
import com.ohdeerit.blog.services.interfaces.PostService;
import com.ohdeerit.blog.services.interfaces.TagService;
import com.ohdeerit.blog.models.entities.CategoryEntity;
import com.ohdeerit.blog.repositories.PostRepository;
import com.ohdeerit.blog.models.entities.UserEntity;
import com.ohdeerit.blog.models.entities.PostEntity;
import com.ohdeerit.blog.models.dtos.CreatePostDto;
import jakarta.persistence.EntityNotFoundException;
import com.ohdeerit.blog.models.entities.TagEntity;
import com.ohdeerit.blog.models.enums.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Slice;
import com.ohdeerit.blog.models.dtos.PostDto;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final CategoryService categoryService;
    private final UserService userService;
    private final TagService tagService;

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
    public Slice<PostDto> getDraftPosts(UUID userId, Pageable pageable) {
        final Slice<PostEntity> postEntities = postRepository.findAllByAuthorIdAndStatus(
                userId, PostStatus.DRAFT, pageable);

        return postEntities.map(postMapper::map);
    }

    @Override
    public PostDto createPost(CreatePostDto post, UUID userId) {
        final CategoryEntity categoryEntity = categoryService.getCategory(post.categoryId());
        final UserEntity userEntity = userService.getUser(userId);
        final List<TagEntity> tagEntities = tagService.getTags(post.tagIds());

        PostEntity newPost = new PostEntity();
        newPost.setCategory(categoryEntity);
        newPost.setAuthor(userEntity);
        newPost.setTitle(post.title());
        newPost.setContent(post.content());
        newPost.setStatus(post.status());
        newPost.setReadingTime(calculateReadTime(post.content()));
        newPost.setTags(new HashSet<>(tagEntities));

        final PostEntity savedPost = postRepository.save(newPost);
        return postMapper.map(savedPost);
    }

    private static Integer calculateReadTime(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        int words = content.split("\\s+").length;

        return (int) Math.ceil((double) words / 200);
    }
}
