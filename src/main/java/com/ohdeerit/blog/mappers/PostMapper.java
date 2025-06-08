package com.ohdeerit.blog.mappers;

import com.ohdeerit.blog.domain.entities.PostEntity;
import com.ohdeerit.blog.domain.dtos.PostDto;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(target = "author", source = "author")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    PostDto map(PostEntity post);

}
