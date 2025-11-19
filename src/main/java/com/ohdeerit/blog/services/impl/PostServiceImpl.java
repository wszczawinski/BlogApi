package com.ohdeerit.blog.services.impl;

import org.springframework.transaction.annotation.Transactional;
import com.ohdeerit.blog.services.mappers.PostServiceMapper;
import org.springframework.beans.factory.annotation.Value;
import com.ohdeerit.blog.repositories.PostMediaRepository;
import org.springframework.web.multipart.MultipartFile;
import com.ohdeerit.blog.repositories.PostRepository;
import com.ohdeerit.blog.config.ThumbnailConstants;
import jakarta.persistence.EntityNotFoundException;
import com.ohdeerit.blog.models.enums.PostStatus;
import org.springframework.data.domain.Pageable;
import com.ohdeerit.blog.services.interfaces.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Slice;
import com.ohdeerit.blog.models.entities.*;
import jakarta.annotation.PostConstruct;
import com.ohdeerit.blog.models.dtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    @Value("${app.post.thumbnail.upload-dir}")
    private String thumbnailUploadDirectory;

    @Value("${app.post.content-files.upload-dir}")
    private String filesUploadDirectory;

    @PostConstruct
    private void init() throws IOException {
        Path thumbnailUploadPath = Paths.get(thumbnailUploadDirectory);
        if (!Files.exists(thumbnailUploadPath)) {
            Files.createDirectories(thumbnailUploadPath);
            log.info("[PostServiceImpl.init] Created upload directory: {}", thumbnailUploadPath);
        } else {
            log.info("[PostServiceImpl.init] Upload directory already exists: {}", thumbnailUploadPath);
        }

        Path thumbnailPath = thumbnailUploadPath.resolve("thumbnail");
        if (!Files.exists(thumbnailPath)) {
            Files.createDirectories(thumbnailPath);
            log.info("[PostServiceImpl.init] Created thumbnail directory: {}", thumbnailPath);
        } else {
            log.info("[PostServiceImpl.init] Thumbnail directory already exists: {}", thumbnailPath);
        }

        Path contentUploadPath = Paths.get(filesUploadDirectory);
        if (!Files.exists(contentUploadPath)) {
            Files.createDirectories(contentUploadPath);
            log.info("[PostServiceImpl.init] Created upload directory: {}", contentUploadPath);
        } else {
            log.info("[PostServiceImpl.init] Upload directory already exists: {}", contentUploadPath);
        }
    }

    private final CategoryService categoryService;
    private final UserService userService;
    private final TagService tagService;
    private final MediaService mediaService;
    private final ImageService imageService;

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;

    private final PostServiceMapper postMapper;

    @Override
    public PostDto getPost(UUID id) {
        final PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No post found with id: " + id));

        return postMapper.map(postEntity);
    }

    @Override
    public PostDto getPostBySlug(String slug) {
        final PostEntity postEntity = postRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with slug: " + slug));

        return postMapper.map(postEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PostDto> getPosts(UUID categoryId, UUID tagId, Pageable pageable) {
        Slice<PostEntity> postEntities;

        if (categoryId != null && tagId != null) {
            CategoryEntity category = categoryService.getCategory(categoryId);
            TagEntity tag = tagService.getTag(tagId);
            postEntities = postRepository.findAllByStatusAndCategoryAndTagsContaining(
                    PostStatus.PUBLISHED, category, tag, pageable);
        } else if (categoryId != null) {
            CategoryEntity category = categoryService.getCategory(categoryId);
            postEntities = postRepository.findAllByStatusAndCategory(
                    PostStatus.PUBLISHED, category, pageable);
        } else if (tagId != null) {
            TagEntity tag = tagService.getTag(tagId);
            postEntities = postRepository.findAllByStatusAndTagsContaining(
                    PostStatus.PUBLISHED, tag, pageable);
        } else {
            postEntities = postRepository.findAllByStatus(PostStatus.PUBLISHED, pageable);
        }

        return postEntities.map(postMapper::map);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PostDto> getAllPosts(Pageable pageable) {
        Slice<PostEntity> posts = postRepository.findAll(pageable);

        return posts.map(postMapper::map);
    }

    @Override
    @Transactional
    public PostDto createPost(final CreatePostDto post, final UUID userId) {
        final CategoryEntity categoryEntity = categoryService.getCategory(post.categoryId());
        final UserEntity userEntity = userService.getUser(userId);

        final var saveThumbnailDto = new SaveImageDto(post.thumbnailFile(), Paths.get(thumbnailUploadDirectory),
                List.of(ThumbnailConstants.POST_THUMBNAIL));
        final String thumbnailFileName = imageService.saveImage(saveThumbnailDto);

        String content = post.content();

        if (!Objects.isNull(post.files())) {
            final Map<String, String> fileUrlMap = parseFileUrls(post.fileUrls());

            Map<String, String> urlReplacements = new HashMap<>();

            for (MultipartFile file : post.files()) {
                final var saveContentFilesDto = new SaveImageDto(file, Paths.get(filesUploadDirectory), null);
                final String contentFileName = imageService.saveImage(saveContentFilesDto);
                log.info("[PostService.createPost] Post content file '{}'", contentFileName);

                final String localFileUrl = fileUrlMap.get(contentFileName);
                if (localFileUrl != null) {
                    String serverUrl = "/resources/post_content/" + contentFileName;
                    urlReplacements.put(localFileUrl, serverUrl);
                }
            }

            content = replaceUrls(post.content(), urlReplacements);
        }

        List<TagEntity> tagEntities = null;
        if (post.tagIds() != null && !post.tagIds().isEmpty()) {
            tagEntities = tagService.getTags(post.tagIds());
        }

        MediaEntity mediaEntity = null;
        if (post.mediaId() != null) {
            mediaEntity = mediaService.getMedia(post.mediaId());
        }

        PostEntity newPost = new PostEntity();
        newPost.setCategory(categoryEntity);
        newPost.setAuthor(userEntity);
        newPost.setTitle(post.title());
        newPost.setContent(content);
        newPost.setStatus(post.status());
        newPost.setReadingTime(calculateReadTime(content));
        newPost.setThumbnail(thumbnailFileName);
        newPost.setShortDescription(post.shortDescription());
        if (tagEntities != null) {
            newPost.setTags(new HashSet<>(tagEntities));
        }

        if (mediaEntity != null) {
            newPost.setMedia(mediaEntity);
        }

        final PostEntity savedPost = postRepository.save(newPost);
        log.info("[PostService.createPost] Post '{}': {}", savedPost.getId(), post.title());

        return postMapper.map(savedPost);
    }


    @Override
    @Transactional
    public PostDto updatePost(final UpdatePostDto updatePost, final UUID userId) {
        PostEntity existingPost = postRepository.findById(updatePost.id())
                .orElseThrow(() -> new EntityNotFoundException("Post with id " + updatePost.id() + " not found"));

        if (updatePost.thumbnailFile() != null) {
            final var saveImageDto = new SaveImageDto(updatePost.thumbnailFile(), Paths.get(thumbnailUploadDirectory),
                    List.of(ThumbnailConstants.POST_THUMBNAIL));
            final String thumbnailFileName = imageService.saveImage(saveImageDto);

            existingPost.setThumbnail(thumbnailFileName);
        }

        String content = updatePost.content();

        if (!Objects.isNull(updatePost.files())) {
            final Map<String, String> fileUrlMap = parseFileUrls(updatePost.fileUrls());

            Map<String, String> urlReplacements = new HashMap<>();

            for (MultipartFile file : updatePost.files()) {
                final var saveContentFilesDto = new SaveImageDto(file, Paths.get(filesUploadDirectory), null);
                final String contentFileName = imageService.saveImage(saveContentFilesDto);
                log.info("[PostService.createPost] Post content file '{}'", contentFileName);

                final String localFileUrl = fileUrlMap.get(contentFileName);
                if (localFileUrl != null) {
                    String serverUrl = "/resources/post_content/" + contentFileName;
                    urlReplacements.put(localFileUrl, serverUrl);
                }
            }

            content = replaceUrls(updatePost.content(), urlReplacements);
        }

        if (updatePost.title() != null) {
            existingPost.setTitle(updatePost.title());
        }

        if (updatePost.shortDescription() != null) {
            existingPost.setShortDescription(updatePost.shortDescription());
        }

        if (updatePost.content() != null) {
            existingPost.setContent(content);
            existingPost.setReadingTime(calculateReadTime(content));
        }

        if (updatePost.categoryId() != null) {
            CategoryEntity category = categoryService.getCategory(updatePost.categoryId());
            existingPost.setCategory(category);
        }

        if (updatePost.tagIds() != null) {
            Set<TagEntity> tags = updatePost.tagIds().stream()
                    .map(tagService::getTag)
                    .collect(Collectors.toSet());
            existingPost.getTags().clear();
            existingPost.getTags().addAll(tags);
        }

        if (updatePost.status() != null) {
            existingPost.setStatus(updatePost.status());
        }

        if (updatePost.mediaId() != null) {
            if (existingPost.getMedia() != null && !existingPost.getMedia().getId().equals(updatePost.mediaId())) {
                postMediaRepository.deleteByPostId(existingPost.getId());
                postMediaRepository.flush();
            }

            final MediaEntity mediaEntity = mediaService.getMedia(updatePost.mediaId());
            existingPost.setMedia(mediaEntity);
        }

        final PostEntity updatedPost = postRepository.save(existingPost);
        return postMapper.map(updatedPost);
    }

    private static Integer calculateReadTime(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        int words = content.split("\\s+").length;

        return (int) Math.ceil((double) words / 200);
    }

    private static Map<String, String> parseFileUrls(final List<String> fileUrls) {
        if (Objects.isNull(fileUrls) || fileUrls.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, String> map = fileUrls.stream()
                .map(entry -> entry.split("' '"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> parts[0].replace("'", "").trim(),
                        parts -> parts[1].replace("'", "").trim()
                ));

        log.debug("[PostService.parseFileUrls] Parsed {} file URL mappings", map.size());
        return map;
    }

    private static String replaceUrls(final String content, final Map<String, String> replacements) {
        String result = content;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            final String localUrl = entry.getKey();
            final String serverUrl = entry.getValue();
            result = result.replace(localUrl, serverUrl);

            log.debug("[PostService.replaceUrls] Replaced '{}' with '{}'", localUrl, serverUrl);
        }

        return result;
    }
}
