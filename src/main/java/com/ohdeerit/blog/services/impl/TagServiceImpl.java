package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.services.mappers.TagServiceMapper;
import com.ohdeerit.blog.services.interfaces.TagService;
import com.ohdeerit.blog.repositories.TagRepository;
import com.ohdeerit.blog.models.entities.TagEntity;
import jakarta.persistence.EntityNotFoundException;
import com.ohdeerit.blog.models.dtos.CreateTagDto;
import org.springframework.stereotype.Service;
import com.ohdeerit.blog.models.dtos.TagDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    private final TagServiceMapper tagMapper;

    @Override
    public List<TagDto> getTags() {
        final List<TagEntity> tagEntities = tagRepository.findAllWithPostCount();

        return tagEntities.stream().map(tagMapper::map).toList();
    }

    @Override
    public List<TagEntity> getTags(Set<UUID> ids) {
        List<TagEntity> tags = tagRepository.findAllById(ids);

        if (tags.size() != ids.size()) {
            throw new EntityNotFoundException("Some of the provided ids do not exist");
        }

        return tags;
    }

    @Override
    public TagEntity getTag(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No tag found with id: " + id));
    }

    @Override
    @Transactional
    public List<TagDto> createTags(Set<CreateTagDto> names) {
        final List<String> tagNames = names.stream().map(CreateTagDto::name).toList();

        final List<TagEntity> existingTags = tagRepository.findByNameIn(tagNames);

        final Set<String> existingTagNames = existingTags.stream()
                .map(TagEntity::getName)
                .collect(Collectors.toSet());

        final List<TagEntity> tagsToCreate = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> TagEntity.builder()
                        .name(name)
                        .posts(new HashSet<>())
                        .build())
                .toList();

        List<TagEntity> savedTags = new ArrayList<>();

        if (!tagsToCreate.isEmpty()) {
            savedTags = tagRepository.saveAll(tagsToCreate);
            log.info("[TagServiceImpl.createTags] Created {} new tags", savedTags.size());
        }

        savedTags.addAll(existingTags);

        return savedTags.stream().map(tagMapper::map).toList();
    }

    @Override
    @Transactional
    public void deleteTag(UUID id) {
        tagRepository.findById(id).ifPresent(tag -> {
            if (!tag.getPosts().isEmpty()) {
                throw new IllegalStateException("Tag with id " + id + " has posts");
            }
            tagRepository.deleteById(id);
            log.info("[TagServiceImpl.deleteTag] Deleted tag with id: {}", id);
        });
    }
}
