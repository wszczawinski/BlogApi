package com.ohdeerit.blog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ohdeerit.blog.models.entities.MediaEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends JpaRepository<MediaEntity, Integer> {
}
