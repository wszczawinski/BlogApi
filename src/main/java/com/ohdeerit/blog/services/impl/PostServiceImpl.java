package com.ohdeerit.blog.services.impl;

import org.springframework.transaction.annotation.Transactional;
import com.ohdeerit.blog.domain.entities.CategoryEntity;
import com.ohdeerit.blog.repositories.PostRepository;
import com.ohdeerit.blog.domain.entities.UserEntity;
import com.ohdeerit.blog.domain.entities.PostEntity;
import com.ohdeerit.blog.domain.dtos.CreatePostDto;
import jakarta.persistence.EntityNotFoundException;
import com.ohdeerit.blog.domain.entities.TagEntity;
import com.ohdeerit.blog.services.CategoryService;
import com.ohdeerit.blog.domain.enums.PostStatus;
import com.ohdeerit.blog.services.UserService;
import org.springframework.stereotype.Service;
import com.ohdeerit.blog.services.PostService;
import com.ohdeerit.blog.services.TagService;
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

    @Override
    public PostEntity getPost(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No post found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostEntity> getPosts(UUID categoryId, UUID tagId) {

        if (categoryId != null && tagId != null) {

            CategoryEntity category = categoryService.getCategory(categoryId);
            TagEntity tag = tagService.getTag(tagId);

            return postRepository.findAllByStatusAndCategoryAndTagsContaining(
                    PostStatus.PUBLISHED,
                    category,
                    tag
            );
        }

        if (categoryId != null) {
            CategoryEntity category = categoryService.getCategory(categoryId);

            return postRepository.findAllByStatusAndCategory(
                    PostStatus.PUBLISHED,
                    category
            );
        }

        if (tagId != null) {
            TagEntity tag = tagService.getTag(tagId);

            return postRepository.findAllByStatusAndTagsContaining(
                    PostStatus.PUBLISHED,
                    tag
            );
        }

        return postRepository.findAllByStatus(PostStatus.PUBLISHED);
    }

    @Override
    public List<PostEntity> getDraftPosts(UUID userId) {
        return postRepository.findAllByAuthorIdAndStatus(userId, PostStatus.DRAFT);
    }

    @Override
    public PostEntity createPost(CreatePostDto post, UUID userId) {
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

        return postRepository.save(newPost);
    }

    private Integer calculateReadTime(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        int words = content.split("\\s+").length;

        return (int) Math.ceil((double) words / 200);
    }
}
