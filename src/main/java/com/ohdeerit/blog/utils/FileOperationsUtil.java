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

    public static Path createMediaDirectory(String uploadDirectory, String folderName) throws IOException {
        Path uploadRoot = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        Path mediaDirectory = uploadRoot.resolve(folderName).normalize();

        if (!mediaDirectory.startsWith(uploadRoot)) {
            throw new SecurityException("Directory traversal attempt detected");
        }

        try {
            Files.createDirectories(mediaDirectory);
            log.debug("Created media directory: {}", mediaDirectory);
            return mediaDirectory;
        } catch (IOException e) {
            throw new IOException("Failed to create media directory: " + mediaDirectory, e);
        }
    }

    public static ProcessedFileInfo saveFileToDirectory(MultipartFile file, Path mediaDirectory) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String cleanFilename = cleanFilename(originalFilename);

        Path filePath = mediaDirectory.resolve(cleanFilename).normalize();
        if (!filePath.startsWith(mediaDirectory)) {
            throw new SecurityException("File path traversal attempt detected");
        }

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Saved file: {} -> {}", originalFilename, cleanFilename);

            return new ProcessedFileInfo(cleanFilename, (int) file.getSize());

        } catch (IOException e) {
            throw new IOException("Failed to save file: " + originalFilename, e);
        }
    }

    public static ProcessedFilesResult processFiles(MultipartFile[] files, String uploadDirectory, String folderName) {
        Path mediaDirectory = null;
        List<ProcessedFileInfo> processedFiles = new ArrayList<>();

        try {
            mediaDirectory = createMediaDirectory(uploadDirectory, folderName);

            for (MultipartFile file : files) {
                ProcessedFileInfo fileInfo = saveFileToDirectory(file, mediaDirectory);
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

    public static void cleanupFiles(ProcessedFilesResult processedFiles) {
        if (processedFiles != null && processedFiles.mediaDirectory() != null) {
            try {
                for (ProcessedFileInfo fileInfo : processedFiles.processedFiles()) {
                    Path filePath = processedFiles.mediaDirectory().resolve(fileInfo.filename());
                    Files.deleteIfExists(filePath);
                }

                cleanupDirectory(processedFiles.mediaDirectory());
            } catch (IOException e) {
                log.warn("Failed to cleanup files: {}", e.getMessage());
            }
        }
    }

    public static void cleanupDirectory(Path directory) {
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

    public static void validateFiles(MultipartFile[] files, long maxFileSize, int maxFiles) {
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

    private static void validateSingleFile(MultipartFile file, long maxFileSize) {
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
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid name");
        }

        if (filename.length() > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException("Filename is too long (max " + MAX_FILENAME_LENGTH + " characters)");
        }

        validateFilenameSecurity(filename);
        validateFileExtension(filename);
        validateMimeType(file);
    }

    private static void validateFilenameSecurity(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename: path traversal detected");
        }

        if (filename.startsWith(".") || filename.endsWith(".")) {
            throw new IllegalArgumentException("Invalid filename: cannot start or end with dot");
        }
    }

    private static void validateFileExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("File must have an extension");
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    String.format("File type '%s' not allowed. Allowed types: %s",
                            extension, String.join(", ", ALLOWED_EXTENSIONS))
            );
        }
    }

    private static void validateMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    String.format("MIME type '%s' not allowed", contentType)
            );
        }
    }

    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }

    private static String cleanFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "file.unknown";
        }

        String cleanName = originalFilename
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_+|_+$", "");

        if (cleanName.isEmpty()) {
            cleanName = "file.unknown";
        }

        return cleanName;
    }

    public record ProcessedFilesResult(Path mediaDirectory, List<ProcessedFileInfo> processedFiles) {
    }

    public record ProcessedFileInfo(String filename, int size) {
    }
}