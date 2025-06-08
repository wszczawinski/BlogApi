package com.ohdeerit.blog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ohdeerit.blog.domain.entities.CategoryEntity;
import com.ohdeerit.blog.domain.entities.PostEntity;
import com.ohdeerit.blog.domain.entities.TagEntity;
import com.ohdeerit.blog.domain.enums.PostStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, UUID> {

    List<PostEntity> findAllByStatusAndCategoryAndTagsContaining(
            PostStatus status,
            CategoryEntity category,
            TagEntity tag);

    List<PostEntity> findAllByStatusAndCategory(
            PostStatus status,
            CategoryEntity category
    );

    List<PostEntity> findAllByStatusAndTagsContaining(
            PostStatus status,
            TagEntity tag
    );

    List<PostEntity> findAllByStatus(
            PostStatus status
    );

    List<PostEntity> findAllByAuthorIdAndStatus(UUID authorId, PostStatus status);

}
