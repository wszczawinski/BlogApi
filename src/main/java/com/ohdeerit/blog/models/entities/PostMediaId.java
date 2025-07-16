package com.ohdeerit.blog.models.entities;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostMediaId implements Serializable {
    private UUID postId;
    private Integer mediaId;
}

