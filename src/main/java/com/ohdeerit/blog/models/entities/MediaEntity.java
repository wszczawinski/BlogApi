package com.ohdeerit.blog.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.util.UUID;

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

    @Column(name = "short", nullable = false)
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

        if (this.shortSlug == null || this.shortSlug.trim().isEmpty()) {
            this.shortSlug = generateSlug(this.short_);
        }

    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            return UUID.randomUUID().toString();
        }

        String slug = name.toLowerCase()
                .replace("ą", "a")
                .replace("ć", "c")
                .replace("ę", "e")
                .replace("ł", "l")
                .replace("ń", "n")
                .replace("ó", "o")
                .replace("ś", "s")
                .replace("ź", "z")
                .replace("ż", "z")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        return slug.length() > 120 ? slug.substring(0, 120) : slug;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MediaEntity that = (MediaEntity) o;
        return type == that.type
                && status == that.status
                && Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(short_, that.short_)
                && Objects.equals(shortSlug, that.shortSlug)
                && Objects.equals(folder, that.folder)
                && Objects.equals(updatedAt, that.updatedAt)
                && Objects.equals(mediaFiles, that.mediaFiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, short_, shortSlug, folder, type, status, updatedAt, mediaFiles);
    }
}
