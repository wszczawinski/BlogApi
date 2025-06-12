package com.ohdeerit.blog.services.mappers;

import com.ohdeerit.blog.models.entities.CategoryEntity;
import com.ohdeerit.blog.models.dtos.CreateCategoryDto;
import com.ohdeerit.blog.models.entities.PostEntity;
import com.ohdeerit.blog.models.dtos.CategoryDto;
import com.ohdeerit.blog.models.enums.PostStatus;
import org.mapstruct.*;

import java.util.Objects;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryServiceMapper {

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

    CategoryEntity map(CreateCategoryDto category);
}
