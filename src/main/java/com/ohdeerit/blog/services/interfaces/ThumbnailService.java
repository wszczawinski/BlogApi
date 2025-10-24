package com.ohdeerit.blog.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface ThumbnailService {

    String create(MultipartFile originalFile);

}
