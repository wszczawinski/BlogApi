package com.ohdeerit.blog.services.impl;

import static com.ohdeerit.blog.utils.FileOperationsUtil.*;
import static com.ohdeerit.blog.utils.ThumbnailUtil.*;

import org.springframework.validation.annotation.Validated;
import com.ohdeerit.blog.services.interfaces.ImageService;
import org.springframework.web.multipart.MultipartFile;
import com.ohdeerit.blog.models.enums.ThumbnailMethod;
import net.coobird.thumbnailator.geometry.Positions;
import com.ohdeerit.blog.models.dtos.ThumbnailDto;
import com.ohdeerit.blog.models.dtos.SaveImageDto;
import org.springframework.stereotype.Service;
import net.coobird.thumbnailator.Thumbnails;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.awt.*;

@Slf4j
@Service
@Validated
public class ImageServiceImpl implements ImageService {

    @Override
    public String saveImage(final SaveImageDto saveImageDto) {
        try {
            final MultipartFile originalFile = saveImageDto.originalFile();
            final Path uploadDirectoryPath = saveImageDto.uploadDirectory();

            final String originalFileName = originalFile.getOriginalFilename();
            if (Objects.isNull(originalFileName)) {
                throw new IllegalArgumentException("Original filename cannot be null");
            }

            final String extension = getFileExtension(originalFileName);

            for (ThumbnailDto thumbnailDto : saveImageDto.thumbnails()) {
                final String hashedFileName = generateImageMd5Hash(originalFileName, thumbnailDto);
                final BufferedImage thumbnail = createThumbnailImage(originalFile, thumbnailDto);

                final String fullHashedFileName = hashedFileName + "." + extension;
                final Path thumbnailPath = uploadDirectoryPath.resolve("thumbnail").resolve(fullHashedFileName);

                ImageIO.write(thumbnail, extension, thumbnailPath.toFile());

                log.info("[ThumbnailServiceImpl.createThumbnail] Created thumbnail: {} -> {}",
                        originalFileName, fullHashedFileName);
            }

            final Path originalPath = uploadDirectoryPath.resolve(originalFileName);

            Files.write(originalPath, originalFile.getBytes());

            log.info("[ThumbnailServiceImpl.createThumbnail] Saved original file: {}", originalFileName);

            return originalFileName;
        } catch (Exception e) {
            log.error("[ThumbnailServiceImpl.createThumbnail] Failed to create thumbnail for file: {}", saveImageDto.originalFile().getOriginalFilename(), e);
            throw new RuntimeException("Failed to create thumbnail", e);
        }
    }

    private BufferedImage createThumbnailImage(final MultipartFile file, final ThumbnailDto thumbnailDto) throws IOException {

        final BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
        final int width = thumbnailDto.width();
        final int height = thumbnailDto.height();
        final ThumbnailMethod method = thumbnailDto.method();
        final int percent = thumbnailDto.percent();

        if (originalImage == null) {
            throw new IOException("Could not read image from file");
        }

        return switch (method) {
            case CROP -> cropImage(originalImage, width, height);
            case FILL -> fillImage(originalImage, width, height);
            case RESIZE, ADAPTIVE -> resizeImage(originalImage, width, height);
            case PERCENT -> resizePercentImage(originalImage, percent);
        };
    }

    private BufferedImage resizeImage(final BufferedImage original, final int width, final int height)
            throws IOException {

        return Thumbnails.of(original).forceSize(width, height).asBufferedImage();
    }

    private BufferedImage resizePercentImage(final BufferedImage original, final int percent) throws IOException {
        double scale = percent / 100.0;

        return Thumbnails.of(original).scale(scale).asBufferedImage();
    }

    private BufferedImage cropImage(final BufferedImage original, final int targetWidth, final int targetHeight)
            throws IOException {
        return Thumbnails.of(original).size(targetWidth, targetHeight).crop(Positions.CENTER).asBufferedImage();
    }

    private BufferedImage fillImage(final BufferedImage original, final int targetWidth, final int targetHeight)
            throws IOException {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        double scaleX = (double) targetWidth / originalWidth;
        double scaleY = (double) targetHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        BufferedImage resized = Thumbnails.of(original).size(scaledWidth, scaledHeight).asBufferedImage();

        BufferedImage canvas = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvas.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;

        g2d.drawImage(resized, x, y, null);
        g2d.dispose();

        return canvas;
    }
}
