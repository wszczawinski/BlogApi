package com.ohdeerit.blog.services.impl;

import com.ohdeerit.blog.models.dtos.*;
import com.ohdeerit.blog.models.entities.*;
import com.ohdeerit.blog.models.enums.PostStatus;
import com.ohdeerit.blog.repositories.PostMediaRepository;
import com.ohdeerit.blog.repositories.PostRepository;
import com.ohdeerit.blog.services.interfaces.*;
import com.ohdeerit.blog.services.mappers.PostServiceMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    // --- constants ---

    private static final UUID POST_ID     = UUID.randomUUID();
    private static final UUID USER_ID     = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    private static final String THUMBNAIL_UPLOAD_DIR = "/tmp/thumbnails";
    private static final String FILES_UPLOAD_DIR     = "/tmp/content";

    private static final String POST_TITLE      = "Test Post";
    private static final String POST_SLUG       = "test-post";
    private static final String POST_CONTENT    = "Some content";
    private static final String POST_SHORT_DESC = "Short desc";
    private static final String THUMBNAIL_NAME  = "thumbnail.jpg";

    private static final String CREATE_TITLE      = "A valid post title";
    private static final String CREATE_SHORT_DESC = "A short description that is long enough";
    private static final String CREATE_CONTENT    = "Post content here for the post";

    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10);

    private static final MockMultipartFile THUMBNAIL = new MockMultipartFile(
            "thumbnail", "thumb.jpg", "image/jpeg", "img".getBytes()
    );

    // --- mocks ---

    @Mock private CategoryService categoryService;
    @Mock private UserService userService;
    @Mock private TagService tagService;
    @Mock private MediaService mediaService;
    @Mock private ImageService imageService;
    @Mock private PostRepository postRepository;
    @Mock private PostMediaRepository postMediaRepository;
    @Mock private PostServiceMapper postMapper;

    @InjectMocks
    private PostServiceImpl postService;

    // --- shared test state ---

    private PostEntity postEntity;
    private PostDto postDto;
    private CategoryEntity categoryEntity;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(postService, "thumbnailUploadDirectory", THUMBNAIL_UPLOAD_DIR);
        ReflectionTestUtils.setField(postService, "filesUploadDirectory", FILES_UPLOAD_DIR);

        categoryEntity = CategoryEntity.builder().id(CATEGORY_ID).name("Tech").build();

        userEntity = new UserEntity();
        userEntity.setId(USER_ID);
        userEntity.setEmail("test@test.com");
        userEntity.setName("Test User");

        postEntity = new PostEntity();
        postEntity.setId(POST_ID);
        postEntity.setTitle(POST_TITLE);
        postEntity.setContent(POST_CONTENT);
        postEntity.setStatus(PostStatus.PUBLISHED);
        postEntity.setReadingTime(1);
        postEntity.setThumbnail(THUMBNAIL_NAME);
        postEntity.setCategory(categoryEntity);
        postEntity.setAuthor(userEntity);
        postEntity.setTags(new HashSet<>());

        postDto = new PostDto(POST_ID, POST_TITLE, POST_SLUG, POST_CONTENT,
                POST_SHORT_DESC, THUMBNAIL_NAME, null, PostStatus.PUBLISHED,
                null, null, Set.of(), 1, null, null);
    }

    // --- getPost ---

    @Test
    @DisplayName("getPost - returns PostDto when post exists")
    void getPost_found() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(postEntity));
        when(postMapper.map(postEntity)).thenReturn(postDto);

        assertThat(postService.getPost(POST_ID)).isEqualTo(postDto);
    }

    @Test
    @DisplayName("getPost - throws EntityNotFoundException when post not found")
    void getPost_notFound() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(POST_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(POST_ID.toString());
        verify(postMapper, never()).map(any(PostEntity.class));
    }

    // --- getPostBySlug ---

    @Test
    @DisplayName("getPostBySlug - returns PostDto when slug exists")
    void getPostBySlug_found() {
        when(postRepository.findBySlug(POST_SLUG)).thenReturn(Optional.of(postEntity));
        when(postMapper.map(postEntity)).thenReturn(postDto);

        assertThat(postService.getPostBySlug(POST_SLUG)).isEqualTo(postDto);
    }

    @Test
    @DisplayName("getPostBySlug - throws EntityNotFoundException when slug not found")
    void getPostBySlug_notFound() {
        when(postRepository.findBySlug(POST_SLUG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostBySlug(POST_SLUG))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(POST_SLUG);
        verify(postMapper, never()).map(any(PostEntity.class));
    }

    // --- getPosts (4 filter branches) ---

    @Test
    @DisplayName("getPosts - no filters returns all published posts")
    void getPosts_noFilters() {
        var slice = new SliceImpl<>(List.of(postEntity), DEFAULT_PAGEABLE, false);
        when(postRepository.findAllByStatus(PostStatus.PUBLISHED, DEFAULT_PAGEABLE)).thenReturn(slice);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        assertThat(postService.getPosts(null, null, DEFAULT_PAGEABLE).getContent()).containsExactly(postDto);
        verify(postRepository).findAllByStatus(PostStatus.PUBLISHED, DEFAULT_PAGEABLE);
    }

    @Test
    @DisplayName("getPosts - category filter only")
    void getPosts_categoryFilter() {
        var slice = new SliceImpl<>(List.of(postEntity), DEFAULT_PAGEABLE, false);
        when(categoryService.getCategory(CATEGORY_ID)).thenReturn(categoryEntity);
        when(postRepository.findAllByStatusAndCategory(PostStatus.PUBLISHED, categoryEntity, DEFAULT_PAGEABLE)).thenReturn(slice);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        assertThat(postService.getPosts(CATEGORY_ID, null, DEFAULT_PAGEABLE).getContent()).containsExactly(postDto);
        verify(postRepository).findAllByStatusAndCategory(PostStatus.PUBLISHED, categoryEntity, DEFAULT_PAGEABLE);
    }

    @Test
    @DisplayName("getPosts - tag filter only")
    void getPosts_tagFilter() {
        UUID tagId = UUID.randomUUID();
        TagEntity tag = TagEntity.builder().id(tagId).name("Java").build();
        var slice = new SliceImpl<>(List.of(postEntity), DEFAULT_PAGEABLE, false);
        when(tagService.getTag(tagId)).thenReturn(tag);
        when(postRepository.findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag, DEFAULT_PAGEABLE)).thenReturn(slice);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        assertThat(postService.getPosts(null, tagId, DEFAULT_PAGEABLE).getContent()).containsExactly(postDto);
        verify(postRepository).findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag, DEFAULT_PAGEABLE);
    }

    @Test
    @DisplayName("getPosts - both category and tag filters")
    void getPosts_categoryAndTagFilter() {
        UUID tagId = UUID.randomUUID();
        TagEntity tag = TagEntity.builder().id(tagId).name("Java").build();
        var slice = new SliceImpl<>(List.of(postEntity), DEFAULT_PAGEABLE, false);
        when(categoryService.getCategory(CATEGORY_ID)).thenReturn(categoryEntity);
        when(tagService.getTag(tagId)).thenReturn(tag);
        when(postRepository.findAllByStatusAndCategoryAndTagsContaining(PostStatus.PUBLISHED, categoryEntity, tag, DEFAULT_PAGEABLE)).thenReturn(slice);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        assertThat(postService.getPosts(CATEGORY_ID, tagId, DEFAULT_PAGEABLE).getContent()).containsExactly(postDto);
        verify(postRepository).findAllByStatusAndCategoryAndTagsContaining(PostStatus.PUBLISHED, categoryEntity, tag, DEFAULT_PAGEABLE);
    }

    // --- getAllPosts ---

    @Test
    @DisplayName("getAllPosts - delegates to repository and maps result")
    void getAllPosts() {
        var page = new PageImpl<>(List.of(postEntity), DEFAULT_PAGEABLE, 1);
        when(postRepository.findAll(DEFAULT_PAGEABLE)).thenReturn(page);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        assertThat(postService.getAllPosts(DEFAULT_PAGEABLE).getContent()).containsExactly(postDto);
    }

    // --- createPost ---

    @Test
    @DisplayName("createPost - creates post without files, tags, or media")
    void createPost_basic() {
        CreatePostDto dto = new CreatePostDto(CREATE_TITLE, CREATE_SHORT_DESC, CREATE_CONTENT,
                CATEGORY_ID, null, PostStatus.DRAFT, null, THUMBNAIL, null, null);

        when(categoryService.getCategory(CATEGORY_ID)).thenReturn(categoryEntity);
        when(userService.getUser(USER_ID)).thenReturn(userEntity);
        when(imageService.saveImage(any())).thenReturn(THUMBNAIL_NAME);
        when(postRepository.save(any())).thenReturn(postEntity);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        assertThat(postService.createPost(dto, USER_ID)).isEqualTo(postDto);
        verify(postRepository).save(any(PostEntity.class));
        verifyNoInteractions(tagService, mediaService);
    }

    @Test
    @DisplayName("createPost - attaches tags when tagIds provided")
    void createPost_withTags() {
        UUID tagId = UUID.randomUUID();
        TagEntity tag = TagEntity.builder().id(tagId).name("Java").build();
        Set<UUID> tagIds = Set.of(tagId);
        CreatePostDto dto = new CreatePostDto(CREATE_TITLE, CREATE_SHORT_DESC, CREATE_CONTENT,
                CATEGORY_ID, tagIds, PostStatus.DRAFT, null, THUMBNAIL, null, null);

        when(categoryService.getCategory(CATEGORY_ID)).thenReturn(categoryEntity);
        when(userService.getUser(USER_ID)).thenReturn(userEntity);
        when(imageService.saveImage(any())).thenReturn(THUMBNAIL_NAME);
        when(tagService.getTags(tagIds)).thenReturn(List.of(tag));
        when(postRepository.save(any())).thenReturn(postEntity);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        postService.createPost(dto, USER_ID);

        verify(tagService).getTags(tagIds);
    }

    @Test
    @DisplayName("createPost - attaches media when mediaId provided")
    void createPost_withMedia() {
        Integer mediaId = 1;
        CreatePostDto dto = new CreatePostDto(CREATE_TITLE, CREATE_SHORT_DESC, CREATE_CONTENT,
                CATEGORY_ID, null, PostStatus.DRAFT, mediaId, THUMBNAIL, null, null);

        when(categoryService.getCategory(CATEGORY_ID)).thenReturn(categoryEntity);
        when(userService.getUser(USER_ID)).thenReturn(userEntity);
        when(imageService.saveImage(any())).thenReturn(THUMBNAIL_NAME);
        when(mediaService.getMedia(mediaId)).thenReturn(new MediaEntity());
        when(postRepository.save(any())).thenReturn(postEntity);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        postService.createPost(dto, USER_ID);

        verify(mediaService).getMedia(mediaId);
    }

    @Test
    @DisplayName("createPost - reading time is calculated from content word count")
    void createPost_readingTimeCalculated() {
        String content = "word ".repeat(400).trim(); // 400 words → ceiling(400/200) = 2 minutes
        CreatePostDto dto = new CreatePostDto(CREATE_TITLE, CREATE_SHORT_DESC, content,
                CATEGORY_ID, null, PostStatus.DRAFT, null, THUMBNAIL, null, null);

        when(categoryService.getCategory(CATEGORY_ID)).thenReturn(categoryEntity);
        when(userService.getUser(USER_ID)).thenReturn(userEntity);
        when(imageService.saveImage(any())).thenReturn(THUMBNAIL_NAME);
        when(postRepository.save(any(PostEntity.class))).thenAnswer(inv -> {
            PostEntity saved = inv.getArgument(0);
            assertThat(saved.getReadingTime()).isEqualTo(2);
            return postEntity;
        });
        when(postMapper.map(postEntity)).thenReturn(postDto);

        postService.createPost(dto, USER_ID);
    }

    @Test
    @DisplayName("createPost - replaces local file URLs in content when files are provided")
    void createPost_withFiles_replacesContentUrls() {
        MockMultipartFile file1 = new MockMultipartFile("files", "local-1.png", "image/png", "f1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "local-2.jpg", "image/jpeg", "f2".getBytes());
        String originalContent = "Image one: blob:http://local/1 and image two: blob:http://local/2";
        CreatePostDto dto = new CreatePostDto(
                CREATE_TITLE,
                CREATE_SHORT_DESC,
                originalContent,
                CATEGORY_ID,
                null,
                PostStatus.DRAFT,
                null,
                THUMBNAIL,
                new MockMultipartFile[]{file1, file2},
                List.of(
                        "'stored-1.png' 'blob:http://local/1'",
                        "'stored-2.jpg' 'blob:http://local/2'"
                )
        );

        when(categoryService.getCategory(CATEGORY_ID)).thenReturn(categoryEntity);
        when(userService.getUser(USER_ID)).thenReturn(userEntity);
        when(imageService.saveImage(any()))
                .thenReturn(THUMBNAIL_NAME, "stored-1.png", "stored-2.jpg");
        when(postRepository.save(any())).thenReturn(postEntity);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        postService.createPost(dto, USER_ID);

        verify(postRepository).save(argThat(saved ->
                saved.getContent().contains("/resources/post_content/stored-1.png")
                        && saved.getContent().contains("/resources/post_content/stored-2.jpg")
        ));
        verify(imageService, times(3)).saveImage(any());
    }

    // --- updatePost ---

    @Test
    @DisplayName("updatePost - throws EntityNotFoundException when post not found")
    void updatePost_notFound() {
        UUID missingId = UUID.randomUUID();
        UpdatePostDto dto = new UpdatePostDto(missingId, "Updated title here",
                "Updated short description long", "Updated content here long",
                CATEGORY_ID, null, PostStatus.PUBLISHED, null, null, null, null);

        when(postRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost(dto, USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }

    @Test
    @DisplayName("updatePost - updates title, content and status")
    void updatePost_basic() {
        UpdatePostDto dto = new UpdatePostDto(POST_ID, "Updated title here",
                "Updated short description long", "Updated content here long",
                null, null, PostStatus.PUBLISHED, null, null, null, null);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(postEntity));
        when(postRepository.save(any())).thenReturn(postEntity);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        assertThat(postService.updatePost(dto, USER_ID)).isEqualTo(postDto);
        assertThat(postEntity.getTitle()).isEqualTo("Updated title here");
        assertThat(postEntity.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        verify(postRepository).save(postEntity);
    }

    @Test
    @DisplayName("updatePost - updates category when categoryId provided")
    void updatePost_withNewCategory() {
        UUID newCategoryId = UUID.randomUUID();
        CategoryEntity newCategory = CategoryEntity.builder().id(newCategoryId).name("Science").build();
        UpdatePostDto dto = new UpdatePostDto(POST_ID, "Updated title here",
                "Updated short description long", "Updated content here long",
                newCategoryId, null, PostStatus.PUBLISHED, null, null, null, null);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(postEntity));
        when(categoryService.getCategory(newCategoryId)).thenReturn(newCategory);
        when(postRepository.save(any())).thenReturn(postEntity);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        postService.updatePost(dto, USER_ID);

        assertThat(postEntity.getCategory()).isEqualTo(newCategory);
        verify(categoryService).getCategory(newCategoryId);
    }

    @Test
    @DisplayName("updatePost - replaces tags when tagIds provided")
    void updatePost_withTags() {
        UUID tagId = UUID.randomUUID();
        TagEntity tag = TagEntity.builder().id(tagId).name("Spring").build();
        postEntity.setTags(new HashSet<>(Set.of(TagEntity.builder().id(UUID.randomUUID()).name("Old").build())));

        UpdatePostDto dto = new UpdatePostDto(POST_ID, "Updated title here",
                "Updated short description long", "Updated content here long",
                null, Set.of(tagId), PostStatus.PUBLISHED, null, null, null, null);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(postEntity));
        when(tagService.getTag(tagId)).thenReturn(tag);
        when(postRepository.save(any())).thenReturn(postEntity);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        postService.updatePost(dto, USER_ID);

        assertThat(postEntity.getTags()).containsExactly(tag);
    }

    @Test
    @DisplayName("updatePost - updates thumbnail when new thumbnail file is provided")
    void updatePost_withThumbnail() {
        MockMultipartFile newThumbnail = new MockMultipartFile(
                "thumbnail",
                "new-thumb.jpg",
                "image/jpeg",
                "new-image".getBytes()
        );
        UpdatePostDto dto = new UpdatePostDto(
                POST_ID,
                "Updated title here",
                "Updated short description long",
                "Updated content here long",
                null,
                null,
                PostStatus.PUBLISHED,
                null,
                newThumbnail,
                null,
                null
        );

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(postEntity));
        when(imageService.saveImage(any())).thenReturn("new-thumb.jpg");
        when(postRepository.save(any())).thenReturn(postEntity);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        postService.updatePost(dto, USER_ID);

        assertThat(postEntity.getThumbnail()).isEqualTo("new-thumb.jpg");
        verify(imageService).saveImage(any());
    }

    @Test
    @DisplayName("updatePost - replaces local file URLs in content when files are provided")
    void updatePost_withFiles_replacesContentUrls() {
        MockMultipartFile file1 = new MockMultipartFile("files", "local-1.png", "image/png", "f1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "local-2.jpg", "image/jpeg", "f2".getBytes());
        String originalContent = "Image one: blob:http://local/1 and image two: blob:http://local/2";
        UpdatePostDto dto = new UpdatePostDto(
                POST_ID,
                "Updated title here",
                "Updated short description long",
                originalContent,
                null,
                null,
                PostStatus.PUBLISHED,
                null,
                null,
                new MockMultipartFile[]{file1, file2},
                List.of(
                        "'stored-1.png' 'blob:http://local/1'",
                        "'stored-2.jpg' 'blob:http://local/2'"
                )
        );

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(postEntity));
        when(imageService.saveImage(any())).thenReturn("stored-1.png", "stored-2.jpg");
        when(postRepository.save(any())).thenReturn(postEntity);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        postService.updatePost(dto, USER_ID);

        assertThat(postEntity.getContent()).contains("/resources/post_content/stored-1.png");
        assertThat(postEntity.getContent()).contains("/resources/post_content/stored-2.jpg");
        verify(imageService, times(2)).saveImage(any());
    }

    @Test
    @DisplayName("updatePost - clears old post-media relation when media changes")
    void updatePost_mediaChanged_clearsRelation() {
        MediaEntity currentMedia = new MediaEntity();
        currentMedia.setId(1);
        postEntity.setMedia(currentMedia);

        MediaEntity newMedia = new MediaEntity();
        newMedia.setId(2);

        UpdatePostDto dto = new UpdatePostDto(
                POST_ID,
                "Updated title here",
                "Updated short description long",
                "Updated content here long",
                null,
                null,
                PostStatus.PUBLISHED,
                2,
                null,
                null,
                null
        );

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(postEntity));
        when(mediaService.getMedia(2)).thenReturn(newMedia);
        when(postRepository.save(any())).thenReturn(postEntity);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        postService.updatePost(dto, USER_ID);

        verify(postMediaRepository).deleteByPostId(POST_ID);
        verify(postMediaRepository).flush();
        verify(mediaService).getMedia(2);
        assertThat(postEntity.getMedia()).isEqualTo(newMedia);
    }

    @Test
    @DisplayName("updatePost - does not clear post-media relation when media id stays the same")
    void updatePost_mediaUnchanged_doesNotClearRelation() {
        MediaEntity currentMedia = new MediaEntity();
        currentMedia.setId(2);
        postEntity.setMedia(currentMedia);

        UpdatePostDto dto = new UpdatePostDto(
                POST_ID,
                "Updated title here",
                "Updated short description long",
                "Updated content here long",
                null,
                null,
                PostStatus.PUBLISHED,
                2,
                null,
                null,
                null
        );

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(postEntity));
        when(mediaService.getMedia(2)).thenReturn(currentMedia);
        when(postRepository.save(any())).thenReturn(postEntity);
        when(postMapper.map(postEntity)).thenReturn(postDto);

        postService.updatePost(dto, USER_ID);

        verify(postMediaRepository, never()).deleteByPostId(any(UUID.class));
        verify(postMediaRepository, never()).flush();
        verify(mediaService).getMedia(2);
    }
}
