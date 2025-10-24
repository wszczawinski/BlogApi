package com.ohdeerit.blog.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;
import lombok.Getter;

@Getter
@RequiredArgsConstructor
public enum ThumbnailMethod {
    CROP("crop"),
    FILL("fill"),
    RESIZE("resize"),
    PERCENT("percent");

    @JsonValue
    private final String value;

    @JsonCreator
    public static ThumbnailMethod fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Method cannot be null");
        }

        for (ThumbnailMethod method : ThumbnailMethod.values()) {
            if (method.value.equalsIgnoreCase(value)) {
                return method;
            }
        }

        throw new IllegalArgumentException("Invalid thumbnail method: " + value +
            ". Valid values are: crop, fill, resize, percent");
    }
}
