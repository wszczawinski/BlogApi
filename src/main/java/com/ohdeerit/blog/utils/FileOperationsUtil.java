package com.ohdeerit.blog.utils;

import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Set;

@Slf4j
public class FileOperationsUtil {

    private static final int MAX_FILENAME_LENGTH = 255;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpg", "image/jpeg", "image/png", "image/gif", "image/webp"
    );

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


    public static void validateFile(final MultipartFile file, final long maxFileSize) {
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
        if (Objects.isNull(filename) || filename.trim().isEmpty()) {
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

    public static String cleanFilename(final String originalFilename) {
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
}
