package com.ohdeerit.blog.domain.dtos;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {

    private UUID id;
    private String name;
    private Integer postCount;

}
