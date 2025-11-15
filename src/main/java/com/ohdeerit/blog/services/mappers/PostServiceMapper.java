package com.ohdeerit.blog.services.mappers;

import com.ohdeerit.blog.models.entities.PostEntity;
import com.ohdeerit.blog.models.dtos.PostDto;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = MediaServiceMapper.class)
public interface PostServiceMapper {

    @Mapping(target = "thumbnail", source = "thumbnailHash")
    PostDto map(PostEntity post);

}
