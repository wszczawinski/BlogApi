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
        final ThumbnailMethod method = ThumbnailMethod.ADAPTIVE;
        final int percent = 85;

        final String result = ThumbnailUtil.generateImageMd5Hash(fileName, width, height, method, percent);

        assertNotNull(result);
        assertEquals(32, result.length());
        assertTrue(result.matches("[a-f0-9]{32}"));

        System.out.println("Input: " + fileName + width + height + method.getValue() + percent);
        System.out.println("Java MD5: " + result);
        System.out.println("Verify with: echo -n '" + fileName + width + height + method.getValue() + percent + "' | md5sum");
    }
}
