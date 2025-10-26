package com.ohdeerit.blog.utils;

import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;

@Slf4j
public class FileOperationsUtil {

    private static final DateTimeFormatter FOLDER_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    private static final int MAX_FILENAME_LENGTH = 255;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    public static String generateFolderName() {
        return LocalDateTime.now().format(FOLDER_NAME_FORMATTER);
    }

    public static Path createMediaDirectory(final String uploadDirectory, final String folderName) throws IOException {
        final Path uploadMediaDirectory = Paths.get(uploadDirectory);
        final Path mediaDirectory = uploadMediaDirectory.resolve(folderName).normalize();

        if (!mediaDirectory.startsWith(uploadMediaDirectory)) {
            throw new SecurityException("Directory traversal attempt detected");
        }

        try {
            Files.createDirectories(mediaDirectory);
            log.debug("[FileOperationsUtil.createMediaDirectory] Created media directory: {}", mediaDirectory);
            return mediaDirectory;
        } catch (IOException e) {
            throw new IOException("Failed to create media directory: " + mediaDirectory, e);
        }
    }

    public static ProcessedFileInfo saveFileToDirectory(final MultipartFile file, final Path mediaDirectory) throws IOException {
        final String originalFilename = file.getOriginalFilename();
        final String cleanFilename = cleanFilename(originalFilename);

        final Path filePath = mediaDirectory.resolve(cleanFilename).normalize();

        if (!filePath.startsWith(mediaDirectory)) {
            throw new SecurityException("File path traversal attempt detected");
        }

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("[FileOperationsUtil.saveFileToDirectory] Saved file: {} -> {}", originalFilename, cleanFilename);

            return new ProcessedFileInfo(cleanFilename, (int) file.getSize());
        } catch (IOException e) {
            throw new IOException("Failed to save file: " + originalFilename, e);
        }
    }

    public static ProcessedFilesResult processFiles(final MultipartFile[] files,
                                                    final String uploadDirectory, final String folderName) {
        Path mediaDirectory = null;
        List<ProcessedFileInfo> processedFiles = new ArrayList<>();

        try {
            mediaDirectory = createMediaDirectory(uploadDirectory, folderName);

            for (MultipartFile file : files) {
                final ProcessedFileInfo fileInfo = saveFileToDirectory(file, mediaDirectory);
                processedFiles.add(fileInfo);
            }

            return new ProcessedFilesResult(mediaDirectory, processedFiles);

        } catch (IOException e) {
            log.error("Failed to process files: {}", e.getMessage());
            cleanupDirectory(mediaDirectory);
            throw new IllegalStateException("Failed to process media files: " + e.getMessage(), e);

        } catch (SecurityException e) {
            log.error("Security violation during file processing: {}", e.getMessage());
            cleanupDirectory(mediaDirectory);
            throw e;
        }
    }

    public static String getFileExtension(final String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        final int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("File must have an extension");
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    public static String getFileNameWithoutExtension(final String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        final int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("File must have an extension");
        }

        return fileName.substring(0, lastDotIndex);
    }

    public static void cleanupFiles(final ProcessedFilesResult processedFiles) {
        if (processedFiles != null && processedFiles.mediaDirectory() != null) {
            try {
                for (ProcessedFileInfo fileInfo : processedFiles.processedFiles()) {
                    final Path filePath = processedFiles.mediaDirectory().resolve(fileInfo.filename());
                    Files.deleteIfExists(filePath);
                }

                cleanupDirectory(processedFiles.mediaDirectory());
            } catch (IOException e) {
                log.warn("Failed to cleanup files: {}", e.getMessage());
            }
        }
    }

    public static void cleanupDirectory(final Path directory) {
        if (directory != null && Files.exists(directory)) {
            try {
                if (Files.list(directory).findAny().isEmpty()) {
                    Files.deleteIfExists(directory);
                    log.debug("Cleaned up empty directory: {}", directory);
                } else {
                    log.warn("Directory not empty, skipping cleanup: {}", directory);
                }
            } catch (IOException e) {
                log.warn("Failed to cleanup directory: {}", directory, e);
            }
        }
    }

    public static void validateFiles(final MultipartFile[] files, final long maxFileSize, final int maxFiles) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("At least one file is required");
        }

        if (files.length > maxFiles) {
            throw new IllegalArgumentException(
                    String.format("Too many files. Maximum allowed: %d, provided: %d", maxFiles, files.length)
            );
        }

        for (int i = 0; i < files.length; i++) {
            try {
                validateSingleFile(files[i], maxFileSize);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(String.format("File %d validation failed: %s", i + 1, e.getMessage()));
            }
        }
    }

    private static void validateSingleFile(final MultipartFile file, final long maxFileSize) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("File '%s' size (%d bytes) exceeds maximum allowed size (%d bytes)",
                            file.getOriginalFilename(), file.getSize(), maxFileSize)
            );
        }

        String filename = file.getOriginalFilename();
        validateFilenameSecurity(filename);
        validateFileExtension(filename);
        validateMimeType(file);
    }

    public static void validateFilenameSecurity(final String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid name");
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename: path traversal detected");
        }

        if (filename.startsWith(".") || filename.endsWith(".")) {
            throw new IllegalArgumentException("Invalid filename: cannot start or end with dot");
        }

        if (filename.length() > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException("Filename is too long (max " + MAX_FILENAME_LENGTH + " characters)");
        }
    }

    private static void validateFileExtension(final String filename) {
        final String extension = getFileExtension(filename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    String.format("File type '%s' not allowed. Allowed types: %s",
                            extension, String.join(", ", ALLOWED_EXTENSIONS))
            );
        }
    }

    private static void validateMimeType(final MultipartFile file) {
        final String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    String.format("MIME type '%s' not allowed", contentType)
            );
        }
    }

    private static String cleanFilename(final String originalFilename) {

        final int lastDotIndex = originalFilename.lastIndexOf('.');
        final String nameWithoutExt = originalFilename.substring(0, lastDotIndex);
        final String extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();

        String cleanName = nameWithoutExt
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_+|_+$", "");

        if (cleanName.isEmpty()) {
            cleanName = "file_" + System.currentTimeMillis();
        }

        return cleanName + "." + extension;
    }

    public record ProcessedFilesResult(Path mediaDirectory, List<ProcessedFileInfo> processedFiles) {
    }

    public record ProcessedFileInfo(String filename, int size) {
    }
}