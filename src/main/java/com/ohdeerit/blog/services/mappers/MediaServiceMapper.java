package com.ohdeerit.blog.services.mappers;

import com.ohdeerit.blog.models.entities.MediaFileEntity;
import com.ohdeerit.blog.models.entities.MediaEntity;
import com.ohdeerit.blog.models.dtos.CreateMediaDto;
import com.ohdeerit.blog.models.dtos.MediaFileDto;
import com.ohdeerit.blog.models.dtos.MediaDto;
import org.springframework.data.domain.Slice;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MediaServiceMapper {

    @Mapping(target = "mediaFiles", source = "savedMediaFiles")
    MediaDto map(MediaEntity entity, List<MediaFileEntity> savedMediaFiles);

    @Mapping(target = "mediaFiles", source = "mediaFiles")
    MediaDto mapToMediaDto(MediaEntity entity);

    default Slice<MediaDto> mapToMediaDtos(Slice<MediaEntity> entities) {
        return entities.map(this::mapToMediaDto);
    }

    MediaFileDto map(MediaFileEntity entity);

    @Mapping(target = "name", constant = "")
    @Mapping(target = "shortDescription", source = "dto.shortDescription")
    @Mapping(target = "status", source = "dto.status")
    @Mapping(target = "type", constant = "1")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mediaFiles", ignore = true)
    @Mapping(target = "shortSlug", ignore = true)
    @Mapping(target = "folder", source = "folderName")
    MediaEntity map(CreateMediaDto dto, String folderName);
}
