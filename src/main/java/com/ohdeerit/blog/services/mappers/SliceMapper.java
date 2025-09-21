package com.ohdeerit.blog.services.mappers;

import com.ohdeerit.blog.api.response.SliceResponse;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Slice;

@Component
public class SliceMapper {

    public <T> SliceResponse<T> toSliceResponse(Slice<T> slice) {
        return new SliceResponse<>(
                slice.getContent(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext(),
                slice.getNumberOfElements(),
                slice.isFirst(),
                slice.isLast()
        );
    }
}
