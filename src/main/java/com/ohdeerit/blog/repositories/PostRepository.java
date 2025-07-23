package com.ohdeerit.blog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ohdeerit.blog.models.entities.CategoryEntity;
import com.ohdeerit.blog.models.entities.PostEntity;
import com.ohdeerit.blog.models.entities.TagEntity;
import com.ohdeerit.blog.models.enums.PostStatus;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, UUID> {
    Slice<PostEntity> findAllByStatus(PostStatus status, Pageable pageable);

    Slice<PostEntity> findAllByStatusAndCategory(PostStatus status, CategoryEntity category, Pageable pageable);

    Slice<PostEntity> findAllByStatusAndTagsContaining(PostStatus status, TagEntity tag, Pageable pageable);

    Slice<PostEntity> findAllByStatusAndCategoryAndTagsContaining(
            PostStatus status,
            CategoryEntity category,
            TagEntity tag,
            Pageable pageable
    );

    Slice<PostEntity> findAllByAuthorIdAndStatus(UUID authorId, PostStatus status, Pageable pageable);

    Optional<PostEntity> findBySlug(String slug);
}
