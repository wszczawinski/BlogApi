package com.ohdeerit.blog.config;

import com.ohdeerit.blog.models.enums.ThumbnailMethod;

public final class ThumbnailConstants {

    private ThumbnailConstants() {
        // Utility class
    }

    public static final int WIDTH = 220;
    public static final int HEIGHT = 160;
    public static final ThumbnailMethod METHOD = ThumbnailMethod.RESIZE;
    public static final int PERCENT = 100;
}
