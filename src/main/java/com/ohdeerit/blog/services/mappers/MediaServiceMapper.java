package com.ohdeerit.blog.services.mappers;

import com.ohdeerit.blog.models.entities.MediaFileEntity;
import com.ohdeerit.blog.models.entities.MediaEntity;
import com.ohdeerit.blog.models.dtos.CreateMediaDto;
import com.ohdeerit.blog.models.dtos.MediaFileDto;
import com.ohdeerit.blog.models.dtos.MediaDto;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MediaServiceMapper {

    @Mapping(target = "shortDescription", source = "short_")
    @Mapping(target = "mediaFiles", source = "mediaFiles")
    MediaDto map(MediaEntity entity);

    @Mapping(target = "shortDescription", source = "short_")
    MediaFileDto map(MediaFileEntity entity);

    @Mapping(target = "name", constant = "")
    @Mapping(target = "short_", source = "shortDescription")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "type", constant = "1")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mediaFiles", ignore = true)
    @Mapping(target = "shortSlug", ignore = true)
    @Mapping(target = "folder", ignore = true)
    MediaEntity map(CreateMediaDto dto);
}
