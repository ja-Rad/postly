package com.jarad.postly.unit.service;

import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.repository.PostRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.service.SearchServiceImpl;
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
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private ProfileRepository profileRepository;
    @InjectMocks
    private SearchServiceImpl searchService;

    private List<Post> twoPosts;
    private List<Profile> twoProfiles;

    @BeforeEach
    void setUp() {
        twoPosts = getTwoPosts();
        twoProfiles = getTwoProfiles();
    }

    @Test
    void returnListOfPageNumbers_ValidTotalPages_ReturnsValidListOfPageNumbers() {
        int totalPages = 10;
        List<Integer> expectedResult = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();

        List<Integer> actualResult = searchService.returnListOfPageNumbers(totalPages);

        assertEquals(expectedResult, actualResult, "Lists should be equal");
    }

    @Test
    void findPaginatedPostsContainingTitleByCreationDateDescending_PagedResponseIsPresent_ReturnsValidPageOfPosts() {
        Page<Post> pagedResponse = new PageImpl<>(twoPosts);
        when(postRepository.findByTitleContainsIgnoreCase(anyString(), any(Pageable.class))).thenReturn(pagedResponse);

        Page<Post> actualResult = searchService.findPaginatedPostsContainingTitleByCreationDateDescending("postTitleExample", 1);

        assertThat(actualResult).isNotEmpty();
        assertEquals(twoPosts, actualResult.getContent(), "Lists of Posts should be equal");
        verify(postRepository, times(1)).findByTitleContainsIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void findPaginatedProfilesContainingUsernameByCreationDateDescending_PagedResponseIsPresent_ReturnsValidPageOfProfiles() {
        Page<Profile> pagedResponse = new PageImpl<>(twoProfiles);
        when(profileRepository.findByUsernameContainsIgnoreCase(anyString(), any(Pageable.class))).thenReturn(pagedResponse);

        Page<Profile> actualResult = searchService.findPaginatedProfilesContainingUsernameByCreationDateDescending("profileUsernameExample", 1);

        assertThat(actualResult).isNotEmpty();
        assertEquals(twoProfiles, actualResult.getContent(), "Lists of Comments should be equal");
        verify(profileRepository, times(1)).findByUsernameContainsIgnoreCase(anyString(), any(Pageable.class));
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
     * Helper method that creates Dummy Test Doubles for the List of Profiles
     *
     * @return List of Profiles
     */
    private List<Profile> getTwoProfiles() {
        Profile profile1 = Profile.builder()
                .id(1L)
                .username("1st Username Example")
                .creationDate(Instant.now())
                .build();
        Profile profile2 = Profile.builder()
                .id(2L)
                .username("2nd Username Example")
                .creationDate(Instant.now())
                .build();

        return Arrays.asList(profile1, profile2);
    }
}