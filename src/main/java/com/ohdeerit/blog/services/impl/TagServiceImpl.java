package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.repositories.TagRepository;
import com.ohdeerit.blog.domain.entities.TagEntity;
import org.springframework.stereotype.Service;
import com.ohdeerit.blog.services.TagService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public List<TagEntity> getTags() {
        return tagRepository.findAllWithPostCount();
    }

    @Override
    @Transactional
    public List<TagEntity> createTags(Set<String> names) {
        final List<TagEntity> existingTags = tagRepository.findByNameIn(names);

        final Set<String> existingTagNames = existingTags.stream()
                .map(TagEntity::getName)
                .collect(Collectors.toSet());

        final List<TagEntity> tagsToCreate = names.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> TagEntity.builder()
                        .name(name)
                        .posts(new HashSet<>())
                        .build())
                .toList();

        List<TagEntity> savedTags = new ArrayList<>();

        if (!tagsToCreate.isEmpty()) {
            savedTags = tagRepository.saveAll(tagsToCreate);
        }

        savedTags.addAll(existingTags);

        return savedTags;
    }

    @Override
    @Transactional
    public void deleteTag(UUID id) {
        tagRepository.findById(id).ifPresent(tag -> {
            if (!tag.getPosts().isEmpty()) {
                throw new IllegalStateException("Tag with id " + id + " has posts");
            }
            tagRepository.deleteById(id);
        });
    }
}
