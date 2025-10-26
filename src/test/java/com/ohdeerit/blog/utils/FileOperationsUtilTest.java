package com.ohdeerit.blog.utils;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationsUtilTest {

    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB
    private static final int MAX_FILES = 10;

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should generate folder name in correct format")
    void testGenerateFolderName() {
        final String folderName = FileOperationsUtil.generateFolderName();

        assertNotNull(folderName);
        assertDoesNotThrow(() -> {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            LocalDateTime.parse(folderName, formatter);
        });
        assertEquals(19, folderName.length()); // yyyy-MM-dd-HH-mm-ss
    }

    @Test
    @DisplayName("Should create media directory successfully")
    void testCreateMediaDirectory() throws IOException {
        final String folderName = "2024-01-15-10-30-00";

        final Path createdPath = FileOperationsUtil.createMediaDirectory(tempDir.toString(), folderName);

        assertTrue(Files.exists(createdPath));
        assertTrue(Files.isDirectory(createdPath));
        assertTrue(createdPath.toString().endsWith(folderName));
    }

    @Test
    @DisplayName("Should throw SecurityException on directory traversal attempt")
    void testCreateMediaDirectoryWithTraversal() {
        final String maliciousFolderName = "../../../etc";

        assertThrows(SecurityException.class, () ->
                FileOperationsUtil.createMediaDirectory(tempDir.toString(), maliciousFolderName)
        );
    }

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
    void testValidateFilesSuccess() {
        final MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        assertDoesNotThrow(() ->
                FileOperationsUtil.validateFiles(new MultipartFile[]{file}, MAX_FILE_SIZE, MAX_FILES)
        );
    }

    @Test
    @DisplayName("Should validate multiple files successfully")
    void testValidateMultipleFilesSuccess() {
        final MockMultipartFile file1 = new MockMultipartFile(
                "file1", "test1.jpg", "image/jpeg", "content1".getBytes()
        );
        final MockMultipartFile file2 = new MockMultipartFile(
                "file2", "test2.png", "image/png", "content2".getBytes()
        );

        assertDoesNotThrow(() ->
                FileOperationsUtil.validateFiles(new MultipartFile[]{file1, file2}, MAX_FILE_SIZE, MAX_FILES)
        );
    }

    @Test
    @DisplayName("Should reject null or empty file array")
    void testValidateFilesNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFiles(null, MAX_FILE_SIZE, MAX_FILES));
        assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFiles(new MultipartFile[]{}, MAX_FILE_SIZE, MAX_FILES));
    }

    @Test
    @DisplayName("Should reject too many files")
    void testValidateFilesTooMany() {
        final MultipartFile[] files = new MultipartFile[11];
        for (int i = 0; i < 11; i++) {
            files[i] = new MockMultipartFile(
                    "file" + i, "test" + i + ".jpg", "image/jpeg", "content".getBytes()
            );
        }

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFiles(files, MAX_FILE_SIZE, MAX_FILES)
        );
        assertTrue(exception.getMessage().contains("Too many files"));
    }

    @Test
    @DisplayName("Should reject empty file")
    void testValidateFilesEmptyFile() {
        final MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[0]
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFiles(new MultipartFile[]{emptyFile}, MAX_FILE_SIZE, MAX_FILES)
        );
        assertTrue(exception.getMessage().contains("File cannot be empty"));
    }

    @Test
    @DisplayName("Should reject file exceeding size limit")
    void testValidateFilesTooLarge() {
        final byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        final MockMultipartFile largeFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", largeContent
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFiles(new MultipartFile[]{largeFile}, MAX_FILE_SIZE, MAX_FILES)
        );
        assertTrue(exception.getMessage().contains("exceeds maximum allowed size"));
    }

    @Test
    @DisplayName("Should reject invalid file extension")
    void testValidateFilesInvalidExtension() {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes()
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFiles(new MultipartFile[]{file}, MAX_FILE_SIZE, MAX_FILES)
        );
        assertTrue(exception.getMessage().contains("not allowed"));
    }

    @Test
    @DisplayName("Should reject invalid MIME type")
    void testValidateFilesInvalidMimeType() {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "application/pdf", "content".getBytes()
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                FileOperationsUtil.validateFiles(new MultipartFile[]{file}, MAX_FILE_SIZE, MAX_FILES)
        );
        assertTrue(exception.getMessage().contains("MIME type"));
    }

    @Test
    @DisplayName("Should save file to directory successfully")
    void testSaveFileToDirectory() throws IOException {
        final Path mediaDir = Files.createDirectory(tempDir.resolve("test-media"));
        final MockMultipartFile file = new MockMultipartFile(
                "file", "test image.jpg", "image/jpeg", "test content".getBytes()
        );

        final FileOperationsUtil.ProcessedFileInfo result =
                FileOperationsUtil.saveFileToDirectory(file, mediaDir);

        assertNotNull(result);
        assertEquals("test_image.jpg", result.filename());
        assertEquals(12, result.size());
        assertTrue(Files.exists(mediaDir.resolve(result.filename())));
    }

    @Test
    @DisplayName("Should process multiple files successfully")
    void testProcessFiles() throws IOException {
        final String folderName = "test-folder";
        final MockMultipartFile file1 = new MockMultipartFile(
                "file1", "test1.jpg", "image/jpeg", "content1".getBytes()
        );
        final MockMultipartFile file2 = new MockMultipartFile(
                "file2", "test2.png", "image/png", "content2".getBytes()
        );

        final FileOperationsUtil.ProcessedFilesResult result = FileOperationsUtil.processFiles(
                new MultipartFile[]{file1, file2}, tempDir.toString(), folderName
        );

        assertNotNull(result);
        assertNotNull(result.mediaDirectory());
        assertEquals(2, result.processedFiles().size());
        assertTrue(Files.exists(result.mediaDirectory()));
        assertTrue(Files.exists(result.mediaDirectory().resolve("test1.jpg")));
        assertTrue(Files.exists(result.mediaDirectory().resolve("test2.png")));
    }

    @Test
    @DisplayName("Should cleanup files successfully")
    void testCleanupFiles() throws IOException {
        final String folderName = "cleanup-test";
        final Path mediaDir = FileOperationsUtil.createMediaDirectory(tempDir.toString(), folderName);

        final Path file1 = Files.createFile(mediaDir.resolve("test1.jpg"));
        final Path file2 = Files.createFile(mediaDir.resolve("test2.png"));

        final FileOperationsUtil.ProcessedFilesResult result = new FileOperationsUtil.ProcessedFilesResult(
                mediaDir,
                java.util.List.of(
                        new FileOperationsUtil.ProcessedFileInfo("test1.jpg", 100),
                        new FileOperationsUtil.ProcessedFileInfo("test2.png", 200)
                )
        );

        FileOperationsUtil.cleanupFiles(result);

        assertFalse(Files.exists(file1));
        assertFalse(Files.exists(file2));
        assertFalse(Files.exists(mediaDir));
    }

    @Test
    @DisplayName("Should cleanup empty directory")
    void testCleanupDirectory() throws IOException {
        final Path emptyDir = Files.createDirectory(tempDir.resolve("empty"));

        FileOperationsUtil.cleanupDirectory(emptyDir);

        assertFalse(Files.exists(emptyDir));
    }

    @Test
    @DisplayName("Should not cleanup non-empty directory")
    void testCleanupDirectoryNotEmpty() throws IOException {
        final Path nonEmptyDir = Files.createDirectory(tempDir.resolve("non-empty"));
        Files.createFile(nonEmptyDir.resolve("file.txt"));

        FileOperationsUtil.cleanupDirectory(nonEmptyDir);

        assertTrue(Files.exists(nonEmptyDir));
    }

    @Test
    @DisplayName("Should handle null directory in cleanup gracefully")
    void testCleanupDirectoryNull() {
        assertDoesNotThrow(() -> FileOperationsUtil.cleanupDirectory(null));
    }

    @Test
    @DisplayName("Should clean filename with special characters")
    void testCleanFilenameSpecialChars() throws IOException {
        final Path mediaDir = Files.createDirectory(tempDir.resolve("clean-test"));

        final MockMultipartFile file = new MockMultipartFile(
                "file", "my photo@2024 (1).jpg", "image/jpeg", "content".getBytes()
        );

        final FileOperationsUtil.ProcessedFileInfo result =
                FileOperationsUtil.saveFileToDirectory(file, mediaDir);

        assertEquals("my_photo_2024_1.jpg", result.filename());
    }

    @Test
    @DisplayName("Should clean filename with unicode characters")
    void testCleanFilenameUnicode() throws IOException {
        final Path mediaDir = Files.createDirectory(tempDir.resolve("unicode-test"));

        final MockMultipartFile file = new MockMultipartFile(
                "file", "über_file.jpg", "image/jpeg", "content".getBytes()
        );

        final FileOperationsUtil.ProcessedFileInfo result =
                FileOperationsUtil.saveFileToDirectory(file, mediaDir);

        assertEquals("ber_file.jpg", result.filename());
    }

    @Test
    @DisplayName("Should handle filename with only special characters")
    void testCleanFilenameOnlySpecialChars() throws IOException {
        final Path mediaDir = Files.createDirectory(tempDir.resolve("special-test"));

        final MockMultipartFile file = new MockMultipartFile(
                "file", "@#$%.jpg", "image/jpeg", "content".getBytes()
        );

        final FileOperationsUtil.ProcessedFileInfo result =
                FileOperationsUtil.saveFileToDirectory(file, mediaDir);

        assertTrue(result.filename().startsWith("file_"));
        assertTrue(result.filename().endsWith(".jpg"));
    }
}
