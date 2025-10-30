package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.services.interfaces.FileOperationsService;
import org.springframework.transaction.annotation.Transactional;
import com.ohdeerit.blog.services.mappers.MediaServiceMapper;
import com.ohdeerit.blog.services.interfaces.MediaService;
import com.ohdeerit.blog.repositories.MediaFileRepository;
import org.springframework.beans.factory.annotation.Value;
import com.ohdeerit.blog.models.entities.MediaFileEntity;
import com.ohdeerit.blog.models.dtos.ProcessedFilesDto;
import org.springframework.web.multipart.MultipartFile;
import com.ohdeerit.blog.repositories.MediaRepository;
import com.ohdeerit.blog.models.entities.MediaEntity;
import com.ohdeerit.blog.models.dtos.CreateMediaDto;
import org.springframework.data.domain.Pageable;
import com.ohdeerit.blog.models.dtos.MediaDto;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Slice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaFileRepository mediaFileRepository;
    private final MediaServiceMapper mediaMapper;
    private final FileOperationsService fileOperationsService;

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

        final ProcessedFilesDto processedFiles = fileOperationsService.processFiles(
                files, uploadDirectory, mediaDirectory
        );

        try {
            return saveToDatabase(mediaDirectory, createMediaDto, processedFiles);
        } catch (Exception e) {
            log.error("Database operation failed, cleaning up files: {}", e.getMessage());
            fileOperationsService.cleanupFiles(processedFiles);
            throw e;
        }
    }

    private MediaDto saveToDatabase(final String folderName,
                                    final CreateMediaDto createMediaDto,
                                    final ProcessedFilesDto processedFiles) {
        final MediaEntity mediaEntity = mediaMapper.map(createMediaDto, folderName);

        final MediaEntity savedMedia = mediaRepository.save(mediaEntity);

        log.debug("[MediaServiceImpl.saveToDatabase] Saved media entity with ID: {}", savedMedia.getId());

        final List<MediaFileEntity> mediaFileEntities = processedFiles.processedFiles().stream()
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
                savedMedia.getId(), processedFiles.processedFiles().size(), folderName);

        return mediaMapper.map(savedMedia, savedMediaFiles);
    }
}
