package com.ohdeerit.blog.services.interfaces;

import com.ohdeerit.blog.models.dtos.ProcessedImagesDto;

import java.io.IOException;
import java.nio.file.Path;

public interface FileOperationsService {
    Path createMediaDirectory(String uploadDirectory, String folderName) throws IOException;

    void cleanupFiles(ProcessedImagesDto processedFiles);

    void cleanupDirectory(Path directory);
}
