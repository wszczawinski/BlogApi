package com.ohdeerit.blog.services.mappers;

import com.ohdeerit.blog.models.entities.PostEntity;
import com.ohdeerit.blog.models.dtos.PostDto;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostServiceMapper {

    @Mapping(target = "author", source = "author")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    PostDto map(PostEntity post);

}
