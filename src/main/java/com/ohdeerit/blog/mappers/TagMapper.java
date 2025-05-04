package com.ohdeerit.blog.mappers;

import com.ohdeerit.blog.domain.entities.PostEntity;
import com.ohdeerit.blog.domain.entities.TagEntity;
import com.ohdeerit.blog.domain.enums.PostStatus;
import com.ohdeerit.blog.domain.dtos.TagResponse;
import org.mapstruct.*;

import java.util.Objects;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {

    @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCount")
    TagResponse map(TagEntity tag);

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
