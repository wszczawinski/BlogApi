package com.ohdeerit.blog.services.mappers;

import com.ohdeerit.blog.models.entities.PostEntity;
import com.ohdeerit.blog.models.entities.TagEntity;
import com.ohdeerit.blog.models.enums.PostStatus;
import com.ohdeerit.blog.models.dtos.TagDto;
import org.mapstruct.*;

import java.util.Objects;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagServiceMapper {

    @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCount")
    TagDto map(TagEntity tag);

    @Named("calculatePostCount")
    default Integer calculatePostCount(Set<PostEntity> posts) {
        if (Objects.isNull(posts)) {
            return 0;
        }

        return (int) posts.stream()
                .filter(post -> PostStatus.PUBLISHED.equals(post.getStatus()))
                .count();
    }

}
