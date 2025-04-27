package com.ohdeerit.blog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ohdeerit.blog.domain.entities.TagEntity;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, UUID> {
}
