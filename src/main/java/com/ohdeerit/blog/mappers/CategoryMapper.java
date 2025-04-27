package com.ohdeerit.blog.mappers;

import com.ohdeerit.blog.domain.dtos.CreateCategoryRequest;
import com.ohdeerit.blog.domain.entities.CategoryEntity;
import com.ohdeerit.blog.domain.entities.PostEntity;
import com.ohdeerit.blog.domain.dtos.CategoryDto;
import com.ohdeerit.blog.domain.enums.PostStatus;
import org.mapstruct.*;

import java.util.Objects;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCount")
    CategoryDto map(CategoryEntity category);

    @Named("calculatePostCount")
    default long calculatePostCount(List<PostEntity> postEntities) {
        if (Objects.isNull(postEntities)) {
            return 0;
        }

        return postEntities.stream()
                .filter(post -> PostStatus.PUBLISHED.equals(post.getStatus()))
                .count();
    }

    CategoryEntity map(CreateCategoryRequest category);
}
