package com.ohdeerit.blog.config;

import com.ohdeerit.blog.models.dtos.ThumbnailDto;
import com.ohdeerit.blog.models.enums.ThumbnailMethod;

import java.util.List;

public final class ThumbnailConstants {

    private ThumbnailConstants() {
    }

    public static final ThumbnailDto POST_THUMBNAIL = new ThumbnailDto(
            220,
            160,
            ThumbnailMethod.RESIZE,
            100
    );

    public static final List<ThumbnailDto> MEDIA_IMAGE_THUMBNAILS = List.of(
            new ThumbnailDto(
                    165,
                    100,
                    ThumbnailMethod.ADAPTIVE,
                    100),
            new ThumbnailDto(
                    600,
                    400,
                    ThumbnailMethod.ADAPTIVE,
                    100)
    );
}
