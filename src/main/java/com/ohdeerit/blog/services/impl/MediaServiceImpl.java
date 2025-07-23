package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.utils.FileOperationsUtil.ProcessedFilesResult;
import org.springframework.transaction.annotation.Transactional;
import com.ohdeerit.blog.services.mappers.MediaServiceMapper;
import com.ohdeerit.blog.services.interfaces.MediaService;
import com.ohdeerit.blog.repositories.MediaFileRepository;
import org.springframework.beans.factory.annotation.Value;
import com.ohdeerit.blog.models.entities.MediaFileEntity;
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
    @Transactional(readOnly = true)
    public Slice<MediaDto> getMedia(Pageable pageable) {
        Slice<MediaEntity> mediaEntities = mediaRepository.findAllBy(pageable);

        return mediaEntities.map(mediaMapper::map);
    }

    @Override
    @Transactional
    public MediaDto createMedia(CreateMediaDto createMediaDto) {
        log.debug("Creating media with short description: {}", createMediaDto.shortDescription());

        FileOperationsUtil.validateFiles(createMediaDto.files(), maxFileSize, maxFiles);

        final String folderName = FileOperationsUtil.generateFolderName();

        final ProcessedFilesResult processedFiles = FileOperationsUtil.processFiles(
                createMediaDto.files(), uploadDirectory, folderName
        );

        try {
            return saveToDatabase(createMediaDto, folderName, processedFiles);
        } catch (Exception e) {
            log.error("Database operation failed, cleaning up files: {}", e.getMessage());
            FileOperationsUtil.cleanupFiles(processedFiles);
            throw e;
        }
    }

    private MediaDto saveToDatabase(CreateMediaDto createMediaDto, String folderName, ProcessedFilesResult processedFiles) {
        MediaEntity mediaEntity = mediaMapper.map(createMediaDto);
        mediaEntity.setFolder(folderName);
        MediaEntity savedMedia = mediaRepository.save(mediaEntity);

        log.debug("Saved media entity with ID: {}", savedMedia.getId());

        List<MediaFileEntity> mediaFileEntities = processedFiles.processedFiles().stream()
                .map(fileInfo -> MediaFileEntity.builder()
                        .mediaId(savedMedia.getId())
                        .file(fileInfo.filename())
                        .short_("")
                        .size(fileInfo.size())
                        .position(0)
                        .build())
                .toList();

        List<MediaFileEntity> savedMediaFiles = mediaFileRepository.saveAll(mediaFileEntities);
        savedMedia.setMediaFiles(savedMediaFiles);

        log.info("Successfully created media (ID: {}) with {} files in folder '{}'",
                savedMedia.getId(), processedFiles.processedFiles().size(), folderName);

        return mediaMapper.map(savedMedia);
    }
}
