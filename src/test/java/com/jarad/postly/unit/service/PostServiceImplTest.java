package com.jarad.postly.unit.service;

import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.embeddable.FollowerPK;
import com.jarad.postly.repository.PostRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.service.PostServiceImpl;
import com.jarad.postly.util.dto.PostDto;
import com.jarad.postly.util.exception.PostNotFoundException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private PostServiceImpl postService;

    private List<Post> twoPosts;
    private Post post;
    private PostDto postDto;
    private Profile profile;

    @BeforeEach
    void setUp() {
        twoPosts = getTwoPosts();
        post = getPost();
        postDto = new PostDto();
        profile = getProfile();
    }

    @Test
    void returnListOfPageNumbers_ValidTotalPages_ReturnsValidListOfPageNumbers() {
        int totalPages = 10;
        List<Integer> expectedResult = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();

        List<Integer> actualResult = postService.returnListOfPageNumbers(totalPages);

        assertEquals(expectedResult, actualResult, "Lists should be equal");
    }

    @Test
    void returnPaginatedPostsByCreationDateDescending_PagedResponseIsPresent_ReturnsValidPageOfPosts() {
        Page<Post> pagedResponse = new PageImpl<>(twoPosts);
        when(postRepository.findAll(any(Pageable.class))).thenReturn(pagedResponse);

        Page<Post> actualResult = postService.returnPaginatedPostsByCreationDateDescending(1);

        assertThat(actualResult).isNotEmpty();
        assertEquals(twoPosts, actualResult.getContent(), "Lists of Posts should be equal");
        verify(postRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void returnPostById_PostIsPresent_ReturnsValidPost() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        Post actualResult = postService.returnPostById(1L);

        assertThat(actualResult).isNotNull();
        assertEquals(post, actualResult, "Posts should be equal");
        verify(postRepository, times(1)).findById(anyLong());
    }

    @Test
    void returnPostById_PostIsEmpty_ThrowsPostNotFoundException() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.returnPostById(1L));
        verify(postRepository, times(1)).findById(anyLong());
    }

    @Test
    void createNewPostAndReturnPostId_ProfileIsPresent_ReturnsPostId() {
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.of(profile));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Long actualResult = postService.createNewPostAndReturnPostId(1L, postDto);

        assertThat(actualResult).isNotNull();
        assertEquals(post.getId(), actualResult, "Post Ids should be equal");
        verify(profileRepository, times(1)).findByUserId(anyLong());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void createNewPostAndReturnPostId_ProfileIsEmpty_ThrowsProfileNotFoundException() {
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> postService.createNewPostAndReturnPostId(1L, postDto));
        verify(profileRepository, times(1)).findByUserId(anyLong());
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    void updateExistingPost_PostIsPresent_UpdatesExistingPost() {
        when(postRepository.findByProfileUserIdAndId(anyLong(), anyLong())).thenReturn(Optional.of(post));

        postService.updateExistingPost(1L, 2L, postDto);

        verify(postRepository, times(1)).findByProfileUserIdAndId(anyLong(), anyLong());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void updateExistingPost_PostIsEmpty_UpdatesExistingPost() {
        when(postRepository.findByProfileUserIdAndId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.updateExistingPost(1L, 2L, postDto));
        verify(postRepository, times(1)).findByProfileUserIdAndId(anyLong(), anyLong());
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    void deleteExistingPost_PostIsPresent_deletesExistingPost() {
        when(postRepository.findByProfileIdAndId(anyLong(), anyLong())).thenReturn(Optional.of(post));

        postService.deleteExistingPost(1L, 1L);

        verify(postRepository, times(1)).findByProfileIdAndId(anyLong(), anyLong());
        verify(postRepository, times(1)).delete(any(Post.class));
    }

    @Test
    void deleteExistingPost_PostIsEmpty_ThrowsPostNotFoundException() {
        when(postRepository.findByProfileIdAndId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.deleteExistingPost(1L, 1L));
        verify(postRepository, times(1)).findByProfileIdAndId(anyLong(), anyLong());
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    void isPostOwnedByUser_WhenPostIsOwnedByUser_ReturnsTrue() {
        when(postRepository.existsByProfileUserIdAndId(anyLong(), anyLong())).thenReturn(true);

        boolean actualResult = postService.isPostOwnedByUser(1L, 2L);

        assertTrue(actualResult, "When Post IS owned by User returns True");
        verify(postRepository, times(1)).existsByProfileUserIdAndId(anyLong(), anyLong());
    }

    @Test
    void isPostOwnedByUser_WhenPostIsNotOwnedByUser_ReturnsFalse() {
        when(postRepository.existsByProfileUserIdAndId(anyLong(), anyLong())).thenReturn(false);

        boolean actualResult = postService.isPostOwnedByUser(1L, 2L);

        assertFalse(actualResult, "When Post IS NOT owned by User returns True");
        verify(postRepository, times(1)).existsByProfileUserIdAndId(anyLong(), anyLong());
    }

    @Test
    void returnAuthorsByUserId_ProfileSetOfAuthorsIdsIsPresent_ReturnsValidSetOfLongs() {
        Set<Long> profileSetOfAuthorIds = profile.getFollowers().stream()
                .map(follower -> follower.getId().getAuthorId())
                .collect(toSet());
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.of(profile));

        Set<Long> actualResult = postService.returnAuthorsByUserId(1L);

        assertThat(actualResult).isNotEmpty();
        assertEquals(profileSetOfAuthorIds, actualResult, "Sets of Author Ids should be equal");
        verify(profileRepository, times(1)).findByUserId(anyLong());

    }

    @Test
    void returnAuthorsByUserId_ProfileIsEmpty_ReturnsEmptySet() {
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        Set<Long> actualResult = postService.returnAuthorsByUserId(1L);

        assertThat(actualResult).isEmpty();
        verify(profileRepository, times(1)).findByUserId(anyLong());
    }

    @Test
    void returnTitleByPostId_PostIsPresent_ReturnsValidTitle() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        String actualResult = postService.returnTitleByPostId(1L);

        assertThat(actualResult).isNotEmpty();
        assertEquals(post.getTitle(), actualResult, "Titles of Profiles should be equal");
        verify(postRepository, times(1)).findById(anyLong());
    }

    @Test
    void returnTitleByPostId_PostIsAbsent_ThrowsPostNotFoundException() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.returnTitleByPostId(1L));
        verify(postRepository, times(1)).findById(anyLong());
    }

    @Test
    void convertToHTMLContent_PostDescriptionIsPresent_ValidString() {
        String testPostDescription = """
                ##This is Header##
                This is Paragraph
                """;

        String expectedPostDescription = """
                <h4>This is Header</h4>
                <p>This is Paragraph</p>
                """;

        String actualResult = postService.convertToHTMLContent(testPostDescription);

        assertEquals(expectedPostDescription, actualResult, "Post Description Strings should be equal");
    }

    /**
     * Helper method that creates Dummy Test Doubles for the List of Posts
     *
     * @return List of Posts
     */
    private List<Post> getTwoPosts() {
        Post post1 = Post.builder()
                .id(1L)
                .title("1st Post Title Example")
                .description("1st Post Description Example")
                .creationDate(Instant.now())
                .build();
        Post post2 = Post.builder()
                .id(1L)
                .title("2nd Post Title Example")
                .description("2nd Post Description Example")
                .creationDate(Instant.now())
                .build();

        return Arrays.asList(post1, post2);
    }

    /**
     * Helper method that creates Dummy Test Double for the Post Entity
     *
     * @return Post object
     */
    private Post getPost() {
        return Post.builder()
                .id(1L)
                .title("Title Example")
                .description("Description Example")
                .creationDate(Instant.now())
                .build();
    }

    /**
     * Helper method that creates Dummy Test Double for the Profile Entity
     *
     * @return Profile object
     */
    private Profile getProfile() {
        return Profile.builder()
                .id(1L)
                .username("JohnDoe")
                .followers(Arrays.asList(getFollower(2L), getFollower(3L)))
                .build();
    }

    /**
     * Helper method that creates Dummy Test Double for the Follower Entity
     *
     * @return Follower object
     */
    private Follower getFollower(Long authorId) {
        return Follower.builder()
                .id(FollowerPK.builder().followerId(1L).authorId(authorId).build())
                .creationDate(Instant.now())
                .build();
    }
}