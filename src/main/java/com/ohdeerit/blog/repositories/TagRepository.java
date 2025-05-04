package com.ohdeerit.blog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.ohdeerit.blog.domain.entities.TagEntity;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, UUID> {

    @Query("SELECT t FROM TagEntity t LEFT JOIN FETCH t.posts")
    List<TagEntity> findAllWithPostCount();

    List<TagEntity> findByNameIn(Set<String> names);

}
