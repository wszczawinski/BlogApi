package com.ohdeerit.blog.utils;

import com.ohdeerit.blog.models.enums.ThumbnailMethod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThumbnailUtilTest {

    @Test
    void testGeneratePhpCompatibleMd5Hash() {
        String fileName = "image.jpg";
        int width = 150;
        int height = 150;
        ThumbnailMethod method = ThumbnailMethod.CROP;
        int percent = 85;

        String result = ThumbnailUtil.generateImageMd5Hash(fileName, width, height, method, percent);

        assertNotNull(result);
        assertEquals(32, result.length());
        assertTrue(result.matches("[a-f0-9]{32}"));

        System.out.println("Input: " + fileName + width + height + method + percent);
        System.out.println("Java MD5: " + result);
        System.out.println("Verify in PHP with: echo md5('" + fileName + width + height + method + percent + "');");
    }

    @Test
    void testGetFileExtension() {
        assertEquals("jpg", ThumbnailUtil.getFileExtension("image.jpg"));
        assertEquals("png", ThumbnailUtil.getFileExtension("photo.png"));
        assertEquals("jpeg", ThumbnailUtil.getFileExtension("picture.jpeg"));

        assertThrows(IllegalArgumentException.class, () -> 
            ThumbnailUtil.getFileExtension("noextension"));
        assertThrows(IllegalArgumentException.class, () -> 
            ThumbnailUtil.getFileExtension(""));
        assertThrows(IllegalArgumentException.class, () -> 
            ThumbnailUtil.getFileExtension(null));
        assertThrows(IllegalArgumentException.class, () -> 
            ThumbnailUtil.getFileExtension("file."));
    }

    @Test
    void testGetFileNameWithoutExtension() {
        assertEquals("image", ThumbnailUtil.getFileNameWithoutExtension("image.jpg"));
        assertEquals("photo", ThumbnailUtil.getFileNameWithoutExtension("photo.png"));
        assertEquals("my.complex.file", ThumbnailUtil.getFileNameWithoutExtension("my.complex.file.jpg"));

        assertThrows(IllegalArgumentException.class, () -> 
            ThumbnailUtil.getFileNameWithoutExtension("noextension"));
        assertThrows(IllegalArgumentException.class, () -> 
            ThumbnailUtil.getFileNameWithoutExtension(""));
        assertThrows(IllegalArgumentException.class, () -> 
            ThumbnailUtil.getFileNameWithoutExtension(null));
    }

    @Test
    void testValidateThumbnailParameters() {
        assertDoesNotThrow(() -> ThumbnailUtil.validateThumbnailParameters(150, 150, ThumbnailMethod.CROP, 85));
        assertDoesNotThrow(() -> ThumbnailUtil.validateThumbnailParameters(1, 1, ThumbnailMethod.RESIZE, 1));
        assertDoesNotThrow(() -> ThumbnailUtil.validateThumbnailParameters(2000, 2000, ThumbnailMethod.FILL, 100));

        assertThrows(IllegalArgumentException.class, () ->
            ThumbnailUtil.validateThumbnailParameters(0, 150, ThumbnailMethod.CROP, 85));
        assertThrows(IllegalArgumentException.class, () ->
            ThumbnailUtil.validateThumbnailParameters(150, 0, ThumbnailMethod.CROP, 85));
        assertThrows(IllegalArgumentException.class, () ->
            ThumbnailUtil.validateThumbnailParameters(2001, 150, ThumbnailMethod.CROP, 85));
        assertThrows(IllegalArgumentException.class, () ->
            ThumbnailUtil.validateThumbnailParameters(150, 2001, ThumbnailMethod.CROP, 85));
        assertThrows(IllegalArgumentException.class, () ->
            ThumbnailUtil.validateThumbnailParameters(150, 150, null, 85));
        assertThrows(IllegalArgumentException.class, () ->
            ThumbnailUtil.validateThumbnailParameters(150, 150, ThumbnailMethod.CROP, 0));
        assertThrows(IllegalArgumentException.class, () ->
            ThumbnailUtil.validateThumbnailParameters(150, 150, ThumbnailMethod.CROP, 101));
    }
}