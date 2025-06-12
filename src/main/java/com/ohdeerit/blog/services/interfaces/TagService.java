package com.ohdeerit.blog.services.interfaces;

import com.ohdeerit.blog.models.dtos.CreateTagDto;
import com.ohdeerit.blog.models.dtos.TagDto;
import com.ohdeerit.blog.models.entities.TagEntity;

import java.util.UUID;
import java.util.List;
import java.util.Set;

public interface TagService {
    List<TagDto> getTags();

    List<TagEntity> getTags(Set<UUID> ids);

    TagEntity getTag(UUID id);

    List<TagDto> createTags(Set<CreateTagDto> names);

    void deleteTag(UUID id);
}
