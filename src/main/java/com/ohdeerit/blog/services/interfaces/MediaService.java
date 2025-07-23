package com.ohdeerit.blog.services.interfaces;

import com.ohdeerit.blog.models.dtos.CreateMediaDto;
import org.springframework.data.domain.Pageable;
import com.ohdeerit.blog.models.dtos.MediaDto;
import org.springframework.data.domain.Slice;

public interface MediaService {

    MediaDto createMedia(CreateMediaDto createMediaDto);

    Slice<MediaDto> getMedia(Pageable pageable);

}
