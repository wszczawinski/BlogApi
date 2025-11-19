package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.services.interfaces.FileOperationsService;
import org.springframework.transaction.annotation.Transactional;
import com.ohdeerit.blog.services.mappers.MediaServiceMapper;
import com.ohdeerit.blog.services.interfaces.ImageService;
import com.ohdeerit.blog.services.interfaces.MediaService;
import com.ohdeerit.blog.repositories.MediaFileRepository;
import org.springframework.beans.factory.annotation.Value;
import com.ohdeerit.blog.models.entities.MediaFileEntity;
import org.springframework.web.multipart.MultipartFile;
import com.ohdeerit.blog.repositories.MediaRepository;
import com.ohdeerit.blog.models.entities.MediaEntity;
import com.ohdeerit.blog.config.ThumbnailConstants;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Slice;
import com.ohdeerit.blog.models.dtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaFileRepository mediaFileRepository;
    private final MediaServiceMapper mediaMapper;
    private final FileOperationsService fileOperationsService;
    private final ImageService imageService;

    @Value("${app.media.upload-dir}")
    private String uploadDirectory;

    private static final DateTimeFormatter DIRECTORY_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    @Override
    public MediaEntity getMedia(final Integer id) {

        return mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No media found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<MediaDto> getMedia(final Pageable pageable) {
        final Slice<MediaEntity> mediaEntities = mediaRepository.findAllBy(pageable);

        return mediaMapper.mapToMediaDtos(mediaEntities);
    }

    @Override
    @Transactional
    public MediaDto createMedia(final CreateMediaDto createMediaDto) {
        final MultipartFile[] files = createMediaDto.files();

        final String mediaDirectory = LocalDateTime.now().format(DIRECTORY_NAME_FORMATTER);

        final ProcessedImagesDto processedFiles = processImages(files, mediaDirectory);

        try {
            return saveToDatabase(mediaDirectory, createMediaDto, processedFiles);
        } catch (Exception e) {
            log.error("Database operation failed, cleaning up files: {}", e.getMessage());
            fileOperationsService.cleanupFiles(processedFiles);
            throw e;
        }
    }

    private ProcessedImagesDto processImages(final MultipartFile[] files, final String mediaDirectory) {
        Path mediaDirectoryPath = null;
        List<ProcessedImageInfoDto> processedFiles = new ArrayList<>();
        try {
            mediaDirectoryPath = fileOperationsService.createMediaDirectory(uploadDirectory, mediaDirectory);

            for (MultipartFile file : files) {
                final String fileName = imageService.saveImage(
                        new SaveImageDto(file, mediaDirectoryPath, ThumbnailConstants.MEDIA_IMAGE_THUMBNAILS)
                );

                processedFiles.add(new ProcessedImageInfoDto(fileName, (int) file.getSize()));
            }

            return new ProcessedImagesDto(mediaDirectoryPath, processedFiles);

        } catch (IOException e) {
            log.error("Failed to process files: {}", e.getMessage());
            fileOperationsService.cleanupDirectory(mediaDirectoryPath);
            throw new IllegalStateException("Failed to process media files: " + e.getMessage(), e);
        }
    }

    private MediaDto saveToDatabase(final String folderName,
                                    final CreateMediaDto createMediaDto,
                                    final ProcessedImagesDto processedFiles) {
        final MediaEntity mediaEntity = mediaMapper.map(createMediaDto, folderName);

        final MediaEntity savedMedia = mediaRepository.save(mediaEntity);

        log.debug("[MediaServiceImpl.saveToDatabase] Saved media entity with ID: {}", savedMedia.getId());

        final List<MediaFileEntity> mediaFileEntities = processedFiles.processedImages().stream()
                .map(fileInfo -> MediaFileEntity.builder()
                        .mediaId(savedMedia.getId())
                        .file(fileInfo.filename())
                        .shortDescription("")
                        .size(fileInfo.size())
                        .position(0)
                        .build())
                .toList();

        final List<MediaFileEntity> savedMediaFiles = mediaFileRepository.saveAll(mediaFileEntities);

        log.info("[MediaServiceImpl.saveToDatabase] Successfully created media (ID: {}) with {} files in folder '{}'",
                savedMedia.getId(), processedFiles.processedImages().size(), folderName);

        return mediaMapper.map(savedMedia, savedMediaFiles);
    }
}
