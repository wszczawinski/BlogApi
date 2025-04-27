package com.ohdeerit.blog.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.Set;

@Entity
@Table(name = "tags")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<PostEntity> posts = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TagEntity tagEntity = (TagEntity) o;
        return Objects.equals(id, tagEntity.id)
                && Objects.equals(name, tagEntity.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
