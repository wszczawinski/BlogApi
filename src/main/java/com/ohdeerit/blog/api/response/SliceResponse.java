package com.ohdeerit.blog.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SliceResponse<T>(
        @JsonProperty("content")
        List<T> content,

        @JsonProperty("currentPage")
        int currentPage,

        @JsonProperty("pageSize")
        int pageSize,

        @JsonProperty("hasNext")
        boolean hasNext,

        @JsonProperty("numberOfElements")
        int numberOfElements,

        @JsonProperty("first")
        boolean first,

        @JsonProperty("last")
        boolean last
) {
}
