package com.ohdeerit.blog.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "post_media")
@IdClass(PostMediaId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostMediaEntity {
    @Id
    @Column(name = "post_id", columnDefinition = "BINARY(16)")
    private UUID postId;

    @Id
    @Column(name = "media_id")
    private Integer mediaId;
}
