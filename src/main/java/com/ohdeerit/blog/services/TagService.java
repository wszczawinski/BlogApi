package com.ohdeerit.blog.services;

import com.ohdeerit.blog.domain.entities.TagEntity;

import java.util.UUID;
import java.util.List;
import java.util.Set;

public interface TagService {
    List<TagEntity> getTags();

    List<TagEntity> createTags(Set<String> names);

    void deleteTag(UUID id);
}
