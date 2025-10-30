package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.services.interfaces.FileOperationsService;
import com.ohdeerit.blog.services.interfaces.ImageService;
import org.springframework.beans.factory.annotation.Value;
import com.ohdeerit.blog.models.dtos.ProcessedFileInfoDto;
import org.springframework.web.multipart.MultipartFile;
import com.ohdeerit.blog.models.dtos.ProcessedFilesDto;
import com.ohdeerit.blog.models.enums.ThumbnailMethod;
import com.ohdeerit.blog.models.dtos.SaveImageDto;
import com.ohdeerit.blog.utils.FileOperationsUtil;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileOperationsServiceImpl implements FileOperationsService {

    @Value("${app.media.max-file-size}")
    private long maxFileSize;

    private final ImageService imageService;

    public Path createMediaDirectory(final String uploadDirectory, final String folderName) throws IOException {
        final Path uploadMediaDirectory = Paths.get(uploadDirectory);
        final Path mediaDirectory = uploadMediaDirectory.resolve(folderName).normalize();
        final Path mediathumbnailPath = mediaDirectory.resolve("thumbnail");
        if (!mediaDirectory.startsWith(uploadMediaDirectory)) {
            throw new SecurityException("Directory traversal attempt detected");
        }

        try {
            Files.createDirectories(mediaDirectory);
            Files.createDirectories(mediathumbnailPath);
            log.info("[FileOperationsService.createMediaDirectory] Created media directory: {}", mediaDirectory);
            log.info("[FileOperationsService.createMediaDirectory] Created media thumbnail directory: {}", mediathumbnailPath);
            return mediaDirectory;
        } catch (IOException e) {
            throw new IOException("Failed to create media directory: " + mediaDirectory, e);
        }
    }

    public ProcessedFileInfoDto saveFileToDirectory(final MultipartFile file, final Path mediaDirectory) {
        final String imageName = imageService.saveImage(new SaveImageDto(file, mediaDirectory,
                165, 100, ThumbnailMethod.ADAPTIVE, 100));

        return new ProcessedFileInfoDto(imageName, (int) file.getSize());
    }

    public ProcessedFilesDto processFiles(final MultipartFile[] files,
                                          final String uploadDirectory, final String folderName) {
        Path mediaDirectory = null;
        List<ProcessedFileInfoDto> processedFiles = new ArrayList<>();

        try {
            mediaDirectory = createMediaDirectory(uploadDirectory, folderName);

            for (MultipartFile file : files) {
                FileOperationsUtil.validateFile(file, maxFileSize);
                final ProcessedFileInfoDto fileInfo = saveFileToDirectory(file, mediaDirectory);
                processedFiles.add(fileInfo);
            }

            return new ProcessedFilesDto(mediaDirectory, processedFiles);

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

    public void cleanupFiles(final ProcessedFilesDto processedFiles) {
        if (processedFiles != null && processedFiles.mediaDirectory() != null) {
            try {
                for (ProcessedFileInfoDto fileInfo : processedFiles.processedFiles()) {
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
