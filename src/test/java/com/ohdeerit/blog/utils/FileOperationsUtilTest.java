package com.ohdeerit.blog.utils;

import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;


import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationsUtilTest {

    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB

    @TempDir
    Path tempDir;



    @Test
    @DisplayName("Should extract file extension correctly")
    void testGetFileExtension() {
        assertEquals("jpg", FileOperationsUtil.getFileExtension("image.jpg"));
        assertEquals("png", FileOperationsUtil.getFileExtension("photo.png"));
        assertEquals("jpeg", FileOperationsUtil.getFileExtension("picture.jpeg"));
        assertEquals("jpg", FileOperationsUtil.getFileExtension("image.JPG")); // lowercase
        assertEquals("png", FileOperationsUtil.getFileExtension("my.complex.file.png"));
    }

    @Test
    @DisplayName("Should throw exception for invalid file extensions")
    void testGetFileExtensionInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.getFileExtension("noextension"));
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.getFileExtension(""));
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.getFileExtension(null));
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.getFileExtension("file."));
    }

    @Test
    @DisplayName("Should extract filename without extension correctly")
    void testGetFileNameWithoutExtension() {
        assertEquals("image", FileOperationsUtil.getFileNameWithoutExtension("image.jpg"));
        assertEquals("photo", FileOperationsUtil.getFileNameWithoutExtension("photo.png"));
        assertEquals("my.complex.file", FileOperationsUtil.getFileNameWithoutExtension("my.complex.file.jpg"));
    }

    @Test
    @DisplayName("Should throw exception for invalid filenames when extracting name")
    void testGetFileNameWithoutExtensionInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.getFileNameWithoutExtension("noextension"));
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.getFileNameWithoutExtension(""));
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.getFileNameWithoutExtension(null));
    }

    @Test
    @DisplayName("Should validate filename security successfully")
    void testValidateFilenameSecurity() {
        assertDoesNotThrow(() -> FileOperationsUtil.validateFilenameSecurity("image.jpg"));
        assertDoesNotThrow(() -> FileOperationsUtil.validateFilenameSecurity("my-photo_2024.png"));
        assertDoesNotThrow(() -> FileOperationsUtil.validateFilenameSecurity("test123.jpeg"));
    }

    @Test
    @DisplayName("Should reject filenames with path traversal")
    void testValidateFilenameSecurityPathTraversal() {
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFilenameSecurity("../../../etc/passwd.jpg"));
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFilenameSecurity("..\\windows\\system32.jpg"));
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFilenameSecurity("/etc/passwd.jpg"));
    }

    @Test
    @DisplayName("Should reject filenames starting or ending with dot")
    void testValidateFilenameSecurityDots() {
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFilenameSecurity(".htaccess"));
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFilenameSecurity("file."));
    }

    @Test
    @DisplayName("Should reject null or empty filenames")
    void testValidateFilenameSecurityNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFilenameSecurity(null));
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFilenameSecurity(""));
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFilenameSecurity("   "));
    }

    @Test
    @DisplayName("Should reject filenames exceeding max length")
    void testValidateFilenameSecurityTooLong() {
        final String longFilename = "a".repeat(256) + ".jpg";

        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFilenameSecurity(longFilename));
    }

    @Test
    @DisplayName("Should validate single file successfully")
    void testValidateFileSuccess() {
        final MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        assertDoesNotThrow(() ->
                FileOperationsUtil.validateFile(file, MAX_FILE_SIZE)
        );
    }

    @Test
    @DisplayName("Should reject empty file")
    void testValidateFileEmpty() {
        final MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[0]
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFile(emptyFile, MAX_FILE_SIZE)
        );
        assertTrue(exception.getMessage().contains("File cannot be empty"));
    }

    @Test
    @DisplayName("Should reject file exceeding size limit")
    void testValidateFileTooLarge() {
        final byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        final MockMultipartFile largeFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", largeContent
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFile(largeFile, MAX_FILE_SIZE)
        );
        assertTrue(exception.getMessage().contains("exceeds maximum allowed size"));
    }

    @Test
    @DisplayName("Should reject invalid file extension")
    void testValidateFileInvalidExtension() {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes()
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFile(file, MAX_FILE_SIZE)
        );
        assertTrue(exception.getMessage().contains("not allowed"));
    }

    @Test
    @DisplayName("Should reject invalid MIME type")
    void testValidateFileInvalidMimeType() {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "application/pdf", "content".getBytes()
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFile(file, MAX_FILE_SIZE)
        );
        assertTrue(exception.getMessage().contains("MIME type"));
    }


    @Test
    @DisplayName("Should clean filename with special characters")
    void testCleanFilenameSpecialChars() {
        final String result = FileOperationsUtil.cleanFilename("my photo@2024 (1).jpg");
        assertEquals("my_photo_2024_1.jpg", result);
    }

    @Test
    @DisplayName("Should clean filename with unicode characters")
    void testCleanFilenameUnicode() {
        final String result = FileOperationsUtil.cleanFilename("über_file.jpg");
        assertEquals("ber_file.jpg", result);
    }

    @Test
    @DisplayName("Should handle filename with only special characters")
    void testCleanFilenameOnlySpecialChars() {
        final String result = FileOperationsUtil.cleanFilename("@#$%.jpg");
        assertTrue(result.startsWith("file_"));
        assertTrue(result.endsWith(".jpg"));
    }
}
