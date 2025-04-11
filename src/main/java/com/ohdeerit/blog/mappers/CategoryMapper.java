package com.ohdeerit.blog.mappers;

import com.ohdeerit.blog.models.controllers.v1.category.CreateCategoryRequest;
import com.ohdeerit.blog.models.entities.CategoryEntity;
import com.ohdeerit.blog.models.entities.PostEntity;
import com.ohdeerit.blog.models.enums.PostStatus;
import com.ohdeerit.blog.models.dtos.CategoryDto;
import org.mapstruct.ReportingPolicy;
import jakarta.annotation.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCount")
    CategoryDto map(CategoryEntity categoryEntity);

    @Nullable
    List<CategoryDto> map(List<CategoryEntity> categories);

    @Named("calculatePostCount")
    default long calculatePostCount(List<PostEntity> postEntities) {
        if (Objects.isNull(postEntities)) {
            return 0;
        }

        return postEntities.stream()
                .filter(post -> PostStatus.PUBLISHED.equals(post.getStatus()))
                .count();
    }

    CategoryDto map(CreateCategoryRequest categoryRequest);

    CategoryEntity map(CategoryDto categoryDto);
}
