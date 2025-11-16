package com.ohdeerit.blog.models.entities;

import com.ohdeerit.blog.config.ThumbnailConstants;
import com.ohdeerit.blog.utils.FileOperationsUtil;
import com.ohdeerit.blog.models.enums.PostStatus;
import com.ohdeerit.blog.utils.ThumbnailUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.Set;

@Entity
@Table(name = "posts")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, name = "short", length = 1000)
    private String shortDescription;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String thumbnail;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Column(nullable = false)
    private Integer readingTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags = new HashSet<>();

    @ManyToOne
    @JoinTable(
            name = "post_media",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "media_id")
    )
    private MediaEntity media;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PostEntity that = (PostEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(title, that.title)
                && Objects.equals(slug, that.slug)
                && Objects.equals(content, that.content)
                && status == that.status
                && Objects.equals(readingTime, that.readingTime)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, status, readingTime, createdAt, updatedAt);
    }

    @PrePersist
    protected void onCreate() {
        final LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.slug == null || this.slug.trim().isEmpty()) {
            this.slug = generateSlug(this.title);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getThumbnailHash() {
        if (thumbnail == null || thumbnail.isEmpty()) {
            return thumbnail;
        }

        String hash = ThumbnailUtil.generateImageMd5Hash(
                thumbnail,
                ThumbnailConstants.POST_THUMBNAIL
        );

        String extension = FileOperationsUtil.getFileExtension(thumbnail);
        return hash + "." + extension;
    }

    private String generateSlug(String title) {
        if (title == null || title.trim().isEmpty()) {
            return UUID.randomUUID().toString();
        }

        return title.toLowerCase()
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
    }
}
