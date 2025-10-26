package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.utils.FileOperationsUtil.ProcessedFilesResult;
import org.springframework.transaction.annotation.Transactional;
import com.ohdeerit.blog.services.mappers.MediaServiceMapper;
import com.ohdeerit.blog.services.interfaces.MediaService;
import com.ohdeerit.blog.repositories.MediaFileRepository;
import org.springframework.beans.factory.annotation.Value;
import com.ohdeerit.blog.models.entities.MediaFileEntity;
import org.springframework.web.multipart.MultipartFile;
import com.ohdeerit.blog.repositories.MediaRepository;
import com.ohdeerit.blog.models.entities.MediaEntity;
import com.ohdeerit.blog.models.dtos.CreateMediaDto;
import com.ohdeerit.blog.utils.FileOperationsUtil;
import org.springframework.data.domain.Pageable;
import com.ohdeerit.blog.models.dtos.MediaDto;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Slice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaFileRepository mediaFileRepository;
    private final MediaServiceMapper mediaMapper;

    @Value("${app.media.upload-dir}")
    private String uploadDirectory;

    @Value("${app.media.max-file-size}")
    private long maxFileSize;

    @Value("${app.media.max-files}")
    private int maxFiles;

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

        FileOperationsUtil.validateFiles(files, maxFileSize, maxFiles);

        final String folderName = FileOperationsUtil.generateFolderName();

        final ProcessedFilesResult processedFiles = FileOperationsUtil.processFiles(
                files, uploadDirectory, folderName
        );

        try {
            return saveToDatabase(createMediaDto, folderName, processedFiles);
        } catch (Exception e) {
            log.error("Database operation failed, cleaning up files: {}", e.getMessage());
            FileOperationsUtil.cleanupFiles(processedFiles);
            throw e;
        }
    }

    private MediaDto saveToDatabase(final CreateMediaDto createMediaDto, final String folderName,
                                    final ProcessedFilesResult processedFiles) {
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
