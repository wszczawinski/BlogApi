package com.ohdeerit.blog.models.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "media_file")
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "media_id", nullable = false)
    private Integer mediaId;

    @Column(nullable = false)
    private String file;

    @Column(name = "short", nullable = false)
    private String shortDescription;

    @Column(nullable = false)
    private Integer size;

    @Column(nullable = false)
    private Integer position;
}
