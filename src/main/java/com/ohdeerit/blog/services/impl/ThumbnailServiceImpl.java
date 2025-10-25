package com.ohdeerit.blog.services.impl;

import static com.ohdeerit.blog.utils.ThumbnailUtil.*;

import com.ohdeerit.blog.services.interfaces.ThumbnailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import com.ohdeerit.blog.models.enums.ThumbnailMethod;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Service;
import net.coobird.thumbnailator.Thumbnails;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.*;

@Slf4j
@Service
public class ThumbnailServiceImpl implements ThumbnailService {

    @Value("${app.thumbnail.upload-dir}")
    private String uploadDirectory;

    @Value("${app.thumbnail.default-width}")
    private int defaultWidth;

    @Value("${app.thumbnail.default-height}")
    private int defaultHeight;

    @Value("${app.thumbnail.default-method}")
    private String defaultMethodStr;

    @Value("${app.thumbnail.default-percent}")
    private int defaultPercent;

    @PostConstruct
    private void init() throws IOException {
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("[ThumbnailServiceImpl.init] Created upload directory: {}", uploadPath);
        } else {
            log.info("[ThumbnailServiceImpl.init] Upload directory already exists: {}", uploadPath);
        }
    }

    @Override
    public String create(final MultipartFile originalFile) {
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("Thumbnail file must not be empty");
        }

        return createThumbnail(originalFile, defaultWidth, defaultHeight,
                ThumbnailMethod.fromString(defaultMethodStr), defaultPercent);
    }

    public String createThumbnail(final MultipartFile originalFile, final int width, final int height,
                                  final ThumbnailMethod method, final int percent) {
        try {
            final String originalFileName = originalFile.getOriginalFilename();
            if (originalFileName == null) {
                throw new IllegalArgumentException("Original filename cannot be null");
            }

            final BufferedImage thumbnail = createThumbnailImage(originalFile, width, height, method, percent);

            final String hashedFileName = generateImageMd5Hash(originalFileName, width, height, method, percent);
            final String extension = getFileExtension(originalFileName);

            final String fullHashedFileName = hashedFileName + "." + extension;

            final Path mediaPath = Paths.get(uploadDirectory).resolve(fullHashedFileName);

            ImageIO.write(thumbnail, extension.toLowerCase(), mediaPath.toFile());

            log.info("[ThumbnailServiceImpl.createThumbnail] Created thumbnail: {} -> {}", originalFileName, fullHashedFileName);
            return fullHashedFileName;

        } catch (Exception e) {
            log.error("[ThumbnailServiceImpl.createThumbnail] Failed to create thumbnail for file: {}", originalFile.getOriginalFilename(), e);
            throw new RuntimeException("Failed to create thumbnail", e);
        }
    }

    private BufferedImage createThumbnailImage(final MultipartFile file, final int width, final int height, final ThumbnailMethod method, final int percent) throws IOException {

        validateThumbnailParameters(width, height, method, percent);

        final BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

        if (originalImage == null) {
            throw new IOException("Could not read image from file");
        }

        return switch (method) {
            case CROP -> cropImage(originalImage, width, height);
            case FILL -> fillImage(originalImage, width, height);
            case RESIZE -> resizeImage(originalImage, width, height);
            case PERCENT -> resizePercentImage(originalImage, percent);
        };
    }

    private BufferedImage resizeImage(final BufferedImage original, final int width, final int height) throws IOException {
        return Thumbnails.of(original)
                .forceSize(width, height)
                .asBufferedImage();
    }

    private BufferedImage resizePercentImage(final BufferedImage original, final int percent) throws IOException {
        double scale = percent / 100.0;

        return Thumbnails.of(original)
                .scale(scale)
                .asBufferedImage();
    }

    private BufferedImage cropImage(final BufferedImage original, final int targetWidth, final int targetHeight) throws IOException {
        return Thumbnails.of(original)
                .size(targetWidth, targetHeight)
                .crop(Positions.CENTER)
                .asBufferedImage();
    }

    private BufferedImage fillImage(final BufferedImage original, final int targetWidth, final int targetHeight) throws IOException {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        double scaleX = (double) targetWidth / originalWidth;
        double scaleY = (double) targetHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        BufferedImage resized = Thumbnails.of(original)
                .size(scaledWidth, scaledHeight)
                .asBufferedImage();

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
