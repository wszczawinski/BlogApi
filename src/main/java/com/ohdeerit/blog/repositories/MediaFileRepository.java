package com.ohdeerit.blog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ohdeerit.blog.models.entities.MediaFileEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFileEntity, Integer> {
}
