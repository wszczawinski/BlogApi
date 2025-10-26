package com.ohdeerit.blog.utils;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

import com.ohdeerit.blog.models.enums.ThumbnailMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThumbnailUtil {

    public static String generateImageMd5Hash(final String fileName, final int width, final int height,
                                              final ThumbnailMethod method, final int percent) {
        try {
            final String input = fileName + width + height + method.getValue().toLowerCase() + percent;

            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] messageDigest = md.digest(input.getBytes());

            StringBuilder sb = new StringBuilder();

            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("[ThumbnailUtil.generateImageMd5Hash] MD5 algorithm not available", e);
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    public static void validateThumbnailParameters(final int width, final int height,
                                                   final ThumbnailMethod method, final int percent) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be greater than 0");
        }

        if (height <= 0) {
            throw new IllegalArgumentException("Height must be greater than 0");
        }

        if (method == null) {
            throw new IllegalArgumentException("Method cannot be null");
        }

        if (percent < 1 || percent > 100) {
            throw new IllegalArgumentException("Percent must be between 1 and 100");
        }
    }
}
