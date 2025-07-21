package com.ohdeerit.blog.services.interfaces;

import com.ohdeerit.blog.models.dtos.CreateMediaDto;
import com.ohdeerit.blog.models.dtos.MediaDto;

public interface MediaService {

    MediaDto createMedia(CreateMediaDto createMediaDto);

}
