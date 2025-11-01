package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.services.interfaces.FileOperationsService;
import com.ohdeerit.blog.models.dtos.ProcessedImageInfoDto;
import com.ohdeerit.blog.models.dtos.ProcessedImagesDto;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileOperationsServiceImpl implements FileOperationsService {

    public Path createMediaDirectory(final String uploadDirectory, final String folderName) throws IOException {
        log.info("[FileOperationsService.createMediaDirectory] Creating media directory: {}", uploadDirectory);

        final Path uploadDirectoryPath = Paths.get(uploadDirectory);
        final Path mediaDirectoryPath = uploadDirectoryPath.resolve(folderName).normalize();
        final Path mediathumbnailPath = mediaDirectoryPath.resolve("thumbnail");

        try {
            Files.createDirectories(mediaDirectoryPath);
            Files.createDirectories(mediathumbnailPath);
            log.info("[FileOperationsService.createMediaDirectory] Created media directory: {}", mediaDirectoryPath);
            log.info("[FileOperationsService.createMediaDirectory] Created media thumbnail directory: {}", mediathumbnailPath);
            return mediaDirectoryPath;
        } catch (IOException e) {
            throw new IOException("Failed to create media directory: " + mediaDirectoryPath, e);
        }
    }

    public void cleanupFiles(final ProcessedImagesDto processedFiles) {
        if (processedFiles != null && processedFiles.mediaDirectory() != null) {
            try {
                for (ProcessedImageInfoDto fileInfo : processedFiles.processedImages()) {
                    final Path filePath = processedFiles.mediaDirectory().resolve(fileInfo.filename());
                    Files.deleteIfExists(filePath);
                }

                cleanupDirectory(processedFiles.mediaDirectory());
            } catch (IOException e) {
                log.warn("Failed to cleanup files: {}", e.getMessage());
            }
        }
    }

    public void cleanupDirectory(final Path directory) {
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
}
