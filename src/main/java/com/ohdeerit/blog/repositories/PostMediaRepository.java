package com.ohdeerit.blog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ohdeerit.blog.models.entities.PostMediaEntity;
import com.ohdeerit.blog.models.entities.PostMediaId;
import org.springframework.stereotype.Repository;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMediaEntity, PostMediaId> {
}
