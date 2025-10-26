package com.ohdeerit.blog.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.ohdeerit.blog.models.enums.ThumbnailMethod;
import org.junit.jupiter.api.Test;

class ThumbnailUtilTest {

    @Test
    void testGeneratePhpCompatibleMd5Hash() {
        final String fileName = "image.jpg";
        final int width = 150;
        final int height = 150;
        final ThumbnailMethod method = ThumbnailMethod.CROP;
        final int percent = 85;

        final String result = ThumbnailUtil.generateImageMd5Hash(fileName, width, height, method, percent);

        assertNotNull(result);
        assertEquals(32, result.length());
        assertTrue(result.matches("[a-f0-9]{32}"));

        System.out.println("Input: " + fileName + width + height + method + percent);
        System.out.println("Java MD5: " + result);
        System.out.println("Verify with: echo -n '" + fileName + width + height + method + percent + "' | md5sum");
    }

    @Test
    void testValidateThumbnailParameters() {
        assertDoesNotThrow(() -> ThumbnailUtil.validateThumbnailParameters(150, 150, ThumbnailMethod.CROP, 85));
        assertDoesNotThrow(() -> ThumbnailUtil.validateThumbnailParameters(1, 1, ThumbnailMethod.RESIZE, 1));
        assertDoesNotThrow(() -> ThumbnailUtil.validateThumbnailParameters(2000, 2000, ThumbnailMethod.FILL, 100));

        assertThrows(IllegalArgumentException.class, () -> ThumbnailUtil.validateThumbnailParameters(0, 150, ThumbnailMethod.CROP, 85));
        assertThrows(IllegalArgumentException.class, () -> ThumbnailUtil.validateThumbnailParameters(150, 0, ThumbnailMethod.CROP, 85));
        assertThrows(IllegalArgumentException.class, () -> ThumbnailUtil.validateThumbnailParameters(150, 150, null, 85));
        assertThrows(IllegalArgumentException.class, () -> ThumbnailUtil.validateThumbnailParameters(150, 150, ThumbnailMethod.CROP, 0));
        assertThrows(IllegalArgumentException.class, () -> ThumbnailUtil.validateThumbnailParameters(150, 150, ThumbnailMethod.CROP, 101));
    }
}
