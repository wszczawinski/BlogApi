package com.ohdeerit.blog.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "media")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name="short", nullable = false)
    private String short_;

    @Column(name = "short_slug", nullable = false, length = 120)
    private String shortSlug;

    @Column(nullable = false)
    private String folder;

    @Column(nullable = false)
    private byte type;

    @Column(nullable = false)
    private byte status;

    @Column(name = "updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany
    @JoinColumn(name = "media_id")
    @OrderBy("position ASC")
    private List<MediaFileEntity> mediaFiles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
