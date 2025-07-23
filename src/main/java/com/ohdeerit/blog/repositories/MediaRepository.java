package com.ohdeerit.blog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ohdeerit.blog.models.entities.MediaEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@Repository
public interface MediaRepository extends JpaRepository<MediaEntity, Integer> {

    Slice<MediaEntity> findAllBy(Pageable pageable);

}
