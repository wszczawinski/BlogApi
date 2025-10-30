package com.ohdeerit.blog.services.interfaces;

import com.ohdeerit.blog.models.dtos.ProcessedFileInfoDto;
import org.springframework.web.multipart.MultipartFile;
import com.ohdeerit.blog.models.dtos.ProcessedFilesDto;

import java.io.IOException;
import java.nio.file.Path;

public interface FileOperationsService {
    Path createMediaDirectory(String uploadDirectory, String folderName) throws IOException;

    ProcessedFileInfoDto saveFileToDirectory(MultipartFile file, Path mediaDirectory) throws IOException;

    ProcessedFilesDto processFiles(MultipartFile[] files, String uploadDirectory, String folderName);

    void cleanupFiles(ProcessedFilesDto processedFiles);

    void cleanupDirectory(Path directory);
}
