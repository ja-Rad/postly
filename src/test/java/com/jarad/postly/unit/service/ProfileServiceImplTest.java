package com.jarad.postly.unit.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.entity.embeddable.FollowerPK;
import com.jarad.postly.repository.CommentRepository;
import com.jarad.postly.repository.FollowerRepository;
import com.jarad.postly.repository.PostRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.repository.RoleRepository;
import com.jarad.postly.repository.UserRepository;
import com.jarad.postly.security.SecurityRole;
import com.jarad.postly.service.ProfileServiceImpl;
import com.jarad.postly.util.dto.ProfileDto;
import com.jarad.postly.util.exception.PostNotFoundException;
import com.jarad.postly.util.exception.ProfileForUserAlreadyExistException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import com.jarad.postly.util.exception.UserNotFoundException;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private FollowerRepository followerRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private ProfileServiceImpl profileService;

    private List<Profile> twoProfiles;
    private List<Post> twoPosts;
    private List<Follower> twoFollowers;
    private List<Comment> twoComments;
    private Profile profile;
    private User user;
    private ProfileDto profileDto;
    private Role role;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        twoProfiles = getTwoProfiles();
        twoPosts = getTwoPosts();
        twoFollowers = getTwoFollowers();
        twoComments = getTwoComments();
        profile = getProfile();
        user = getUser();
        profileDto = new ProfileDto();
        role = getRole();
        post = getPost();
        comment = getComment();
    }

    @Test
    void returnPaginatedProfilesByCreationDateDescending_PagedResponseIsPresent_ReturnsValidPageOfProfiles() {
        Page<Profile> pagedResponse = new PageImpl<>(twoProfiles);
        when(profileRepository.findAll(any(Pageable.class))).thenReturn(pagedResponse);

        Page<Profile> actualResult = profileService.returnPaginatedProfilesByCreationDateDescending(1);

        assertThat(actualResult).isNotEmpty();
        assertEquals(twoProfiles, actualResult.getContent(), "Lists of Profiles should be equal");
        verify(profileRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void returnProfilePaginatedPostsByCreationDateDescending_PagedResponseIsPresent_ReturnsValidPageOfPosts() {
        Page<Post> pagedResponse = new PageImpl<>(twoPosts);
        when(postRepository.findPostPageByProfileId(anyLong(), any(Pageable.class))).thenReturn(pagedResponse);

        Page<Post> actualResult = profileService.returnProfilePaginatedPostsByCreationDateDescending(1L, 1);

        assertThat(actualResult).isNotEmpty();
        assertEquals(twoPosts, actualResult.getContent(), "Lists of Posts should be equal");
        verify(postRepository, times(1)).findPostPageByProfileId(anyLong(), any(Pageable.class));
    }

    @Test
    void returnProfilePaginatedAuthorsByCreationDateDescending_PagedResponseIsPresent_ReturnsValidPageOfFollowers() {
        Page<Follower> pagedResponse = new PageImpl<>(twoFollowers);
        when(followerRepository.findAuthorPageByIdFollowerId(anyLong(), any(Pageable.class))).thenReturn(pagedResponse);

        Page<Follower> actualResult = profileService.returnProfilePaginatedAuthorsByCreationDateDescending(1L, 1);

        assertThat(actualResult).isNotEmpty();
        assertEquals(twoFollowers, actualResult.getContent(), "Lists of Followers should be equal");
        verify(followerRepository, times(1)).findAuthorPageByIdFollowerId(anyLong(), any(Pageable.class));
    }

    @Test
    void returnProfilePaginatedFollowersByCreationDateDescending_PagedResponseIsPresent_ReturnsValidPageOfFollowers() {
        Page<Follower> pagedResponse = new PageImpl<>(twoFollowers);
        when(followerRepository.findFollowerPageByIdAuthorId(anyLong(), any(Pageable.class))).thenReturn(pagedResponse);

        Page<Follower> actualResult = profileService.returnProfilePaginatedFollowersByCreationDateDescending(1L, 1);

        assertThat(actualResult).isNotEmpty();
        assertEquals(twoFollowers, actualResult.getContent(), "Lists of Followers should be equal");
        verify(followerRepository, times(1)).findFollowerPageByIdAuthorId(anyLong(), any(Pageable.class));
    }

    @Test
    void returnProfilePaginatedCommentsByCreationDateDescending_PagedResponseIsPresent_ReturnsValidPageOfComments() {
        Page<Comment> pagedResponse = new PageImpl<>(twoComments);
        when(commentRepository.findCommentPageByProfileId(anyLong(), any(Pageable.class))).thenReturn(pagedResponse);

        Page<Comment> actualResult = profileService.returnProfilePaginatedCommentsByCreationDateDescending(1L, 1);

        assertThat(actualResult).isNotEmpty();
        assertEquals(twoComments, actualResult.getContent(), "Lists of Comments should be equal");
        verify(commentRepository, times(1)).findCommentPageByProfileId(anyLong(), any(Pageable.class));
    }

    @Test
    void returnListOfPageNumbers_ValidTotalPages_ReturnsValidListOfPageNumbers() {
        int totalPages = 10;
        List<Integer> expectedResult = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();

        List<Integer> actualResult = profileService.returnListOfPageNumbers(totalPages);

        assertEquals(expectedResult, actualResult, "Lists should be equal");
    }

    @Test
    void returnProfileById_ProfileIsPresent_ReturnsValidProfile() {
        when(profileRepository.findById(anyLong())).thenReturn(Optional.of(profile));

        Profile actualResult = profileService.returnProfileById(1L);

        assertThat(actualResult).isNotNull();
        assertEquals(profile, actualResult, "Profiles should be equal");
        verify(profileRepository, times(1)).findById(anyLong());
    }

    @Test
    void returnProfileById_ProfileIsEmpty_ThrowsProfileNotFoundException() {
        when(profileRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> profileService.returnProfileById(1L));
        verify(profileRepository, times(1)).findById(anyLong());
    }

    @Test
    void createNewProfile_UserIsPresentAndProfileIsEmpty_CreatesNewProfile() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(role));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        profileService.createNewProfile(1L, profileDto);

        verify(userRepository, times(1)).findById(anyLong());
        verify(profileRepository, times(1)).findByUserId(anyLong());
        verify(roleRepository, times(1)).findByName(anyString());
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    void createNewProfile_UserAndProfileArePresent_ThrowsProfileForUserAlreadyExistException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.of(profile));

        assertThrows(ProfileForUserAlreadyExistException.class, () -> profileService.createNewProfile(1L, profileDto));
        verify(userRepository, times(1)).findById(anyLong());
        verify(profileRepository, times(1)).findByUserId(anyLong());
        verifyNoMoreInteractions(profileRepository);
    }

    @Test
    void createNewProfile_UserIsEmpty_ThrowsProfileForUserAlreadyExistException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> profileService.createNewProfile(1L, profileDto));
        verify(userRepository, times(1)).findById(anyLong());
        verifyNoInteractions(profileRepository);
    }

    @Test
    void updateExistingProfile_ProfileIsPresent_UpdatesExistingProfile() {
        when(profileRepository.findByUserIdAndId(anyLong(), anyLong())).thenReturn(Optional.of(profile));

        profileService.updateExistingProfile(1L, 2L, profileDto);

        verify(profileRepository, times(1)).findByUserIdAndId(anyLong(), anyLong());
        verify(profileRepository, times(1)).save(profile);
    }

    @Test
    void updateExistingProfile_ProfileIsEmpty_ThrowsPostNotFoundException() {
        when(profileRepository.findByUserIdAndId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> profileService.updateExistingProfile(1L, 2L, profileDto));
        verify(profileRepository, times(1)).findByUserIdAndId(anyLong(), anyLong());
        verifyNoMoreInteractions(profileRepository);
    }

    @Test
    void deleteExistingProfile_UserAndProfileArePresent_DeletesExistingProfile() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(role));
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.of(profile));

        profileService.deleteExistingProfile(1L, 2L);

        verify(userRepository, times(1)).findById(anyLong());
        verify(roleRepository, times(1)).findByName(anyString());
        verify(profileRepository, times(1)).findByUserId(anyLong());
        verify(profileRepository, times(1)).deleteByUserAndId(eq(user), anyLong());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteExistingProfile_UserIsPresentAndProfileIsEmpty_ThrowsProfileNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> profileService.deleteExistingProfile(1L, 2L));
        verify(userRepository, times(1)).findById(anyLong());
        verify(profileRepository, times(1)).findByUserId(anyLong());
        verifyNoMoreInteractions(roleRepository, profileRepository);
    }

    @Test
    void deleteExistingProfile_UserAndProfileIsEmpty_ThrowsUserNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> profileService.deleteExistingProfile(1L, 2L));
        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(roleRepository, profileRepository);
    }

    @Test
    void isProfileExistForUser_WhenProfileExistForUser_ReturnsTrue() {
        when(profileRepository.existsByUserId(anyLong())).thenReturn(true);

        boolean actualResult = profileService.isProfileExistForUser(1L);

        assertTrue(actualResult, "When Profile EXIST for User returns True");
    }

    @Test
    void isProfileExistForUser_WhenProfileDoesNotExistForUser_ReturnsFalse() {
        when(profileRepository.existsByUserId(anyLong())).thenReturn(false);

        boolean actualResult = profileService.isProfileExistForUser(1L);

        assertFalse(actualResult, "When Profile DOES NOT EXIST for User returns False");
    }

    @Test
    void isUserOwnsThisProfile_WhenUserOwnsThisProfile_ReturnsTrue() {
        when(profileRepository.existsByUserIdAndId(anyLong(), anyLong())).thenReturn(true);

        boolean actualResult = profileService.isUserOwnsThisProfile(1L, 2L);

        assertTrue(actualResult, "When User OWNS this Profile Returns True");
    }

    @Test
    void isUserOwnsThisProfile_WhenUserDoesNotOwnThisProfile_ReturnsFalse() {
        when(profileRepository.existsByUserIdAndId(anyLong(), anyLong())).thenReturn(false);

        boolean actualResult = profileService.isUserOwnsThisProfile(1L, 2L);

        assertFalse(actualResult, "When User DOES NOT OWN this Profile Returns False");
    }

    @Test
    void returnAuthorsByUserId_ProfileSetOfAuthorsIdsIsPresent_ReturnsValidSetOfLongs() {
        Set<Long> profileSetOfAuthorIds = profile.getFollowers().stream()
                .map(follower -> follower.getId().getAuthorId())
                .collect(toSet());
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.of(profile));

        Set<Long> actualResult = profileService.returnAuthorsByUserId(1L);

        assertThat(actualResult).isNotEmpty();
        assertEquals(profileSetOfAuthorIds, actualResult, "Sets of Author Ids should be equal");
        verify(profileRepository, times(1)).findByUserId(anyLong());

    }

    @Test
    void returnAuthorsByUserId_ProfileIsEmpty_ReturnsEmptySet() {
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        Set<Long> actualResult = profileService.returnAuthorsByUserId(1L);

        assertThat(actualResult).isEmpty();
        verify(profileRepository, times(1)).findByUserId(anyLong());
    }

    @Test
    void getOrCreateRole_RoleIsPresent_ReturnsValidRole() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(role));

        Role actualResult = profileService.getOrCreateRole(SecurityRole.ROLE_USER);

        assertThat(actualResult).isNotNull();
        assertEquals(role, actualResult, "Roles should be equal");
    }

    @Test
    void getOrCreateRole_RoleIsEmpty_ReturnsNewSavedRole() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role actualResult = profileService.getOrCreateRole(SecurityRole.ROLE_USER);

        assertThat(actualResult).isNotNull();
        verify(roleRepository).save(any(Role.class));
        assertEquals(role, actualResult, "Roles should be equal");
    }

    @Test
    void returnLatestPostById_PostIsPresent_ReturnsValidPost() {
        when(postRepository.findFirstByProfileIdOrderByIdDesc(anyLong())).thenReturn(Optional.of(post));

        Post actualResult = profileService.returnLatestPostById(1L);

        assertThat(actualResult).isNotNull();
        assertEquals(post, actualResult, "Posts should be equal");
        verify(postRepository, times(1)).findFirstByProfileIdOrderByIdDesc(anyLong());
    }

    @Test
    void returnLatestPostById_PostIsEmpty_ReturnsNull() {
        when(postRepository.findFirstByProfileIdOrderByIdDesc(anyLong())).thenReturn(Optional.empty());

        Post actualResult = profileService.returnLatestPostById(1L);

        assertThat(actualResult).isNull();
        verify(postRepository, times(1)).findFirstByProfileIdOrderByIdDesc(anyLong());
    }

    @Test
    void returnLatestCommentById_CommentIsPresent_ReturnsValidComment() {
        when(commentRepository.findFirstByProfileIdOrderByIdDesc(anyLong())).thenReturn(Optional.of(comment));

        Comment actualResult = profileService.returnLatestCommentById(1L);

        assertThat(actualResult).isNotNull();
        assertEquals(comment, actualResult, "Posts should be equal");
        verify(commentRepository, times(1)).findFirstByProfileIdOrderByIdDesc(anyLong());
    }

    @Test
    void returnLatestCommentById_CommentIsEmpty_ReturnsNull() {
        when(commentRepository.findFirstByProfileIdOrderByIdDesc(anyLong())).thenReturn(Optional.empty());

        Comment actualResult = profileService.returnLatestCommentById(1L);

        assertThat(actualResult).isNull();
        verify(commentRepository, times(1)).findFirstByProfileIdOrderByIdDesc(anyLong());
    }

    @Test
    void returnProfileUsername_ProfileIsPresent_ReturnsValidString() {
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.of(profile));

        String actualResult = profileService.returnProfileUsername(1L);

        assertThat(actualResult).isNotEmpty();
        assertEquals(profile.getUsername(), actualResult, "Profile Usernames should be equal");
        verify(profileRepository, times(1)).findByUserId(anyLong());
    }
    
    @Test
    void returnProfileUsername_ProfileIsEmpty_ThrowsProfileNotFoundException() {
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> profileService.returnProfileUsername(1L));
        verify(profileRepository, times(1)).findByUserId(anyLong());
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

    /**
     * Helper method that creates Dummy Test Doubles for the List of Posts
     *
     * @return List of Posts
     */
    private List<Post> getTwoPosts() {
        Post post1 = Post.builder()
                .id(1L)
                .title("1st Title Example")
                .description("1st Description Example")
                .creationDate(Instant.now())
                .build();
        Post post2 = Post.builder()
                .id(2L)
                .title("2nd Title Example")
                .description("2nd Description Example")
                .creationDate(Instant.now())
                .build();
        return Arrays.asList(post1, post2);
    }

    /**
     * Helper method that creates Dummy Test Doubles for the List of Followers
     *
     * @return List of Followers
     */
    private List<Follower> getTwoFollowers() {
        Follower follower1 = Follower.builder()
                .id(FollowerPK.builder().followerId(1L).authorId(2L).build())
                .creationDate(Instant.now())
                .build();
        Follower follower2 = Follower.builder()
                .id(FollowerPK.builder().followerId(3L).authorId(4L).build())
                .creationDate(Instant.now())
                .build();

        return Arrays.asList(follower1, follower2);
    }

    /**
     * Helper method that creates Dummy Test Doubles for the List of Comments
     *
     * @return List of Comments
     */
    private List<Comment> getTwoComments() {
        Comment comment1 = Comment.builder()
                .id(1L)
                .description("1st Comment Description Example")
                .creationDate(Instant.now())
                .build();
        Comment comment2 = Comment.builder()
                .id(2L)
                .description("2nd Comment Description Example")
                .creationDate(Instant.now())
                .build();

        return Arrays.asList(comment1, comment2);
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

    /**
     * Helper method that creates Dummy Test Double for the User Entity
     *
     * @return User object
     */
    private User getUser() {
        return User.builder()
                .id(1L)
                .email("useremail@example.com")
                .password("User password Example")
                .build();
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
     * Helper method that creates Dummy Test Double for the Comment Entity
     *
     * @return Comment object
     */
    private Comment getComment() {
        return Comment.builder()
                .id(1L)
                .description("Description Example")
                .creationDate(Instant.now())
                .build();
    }

    /**
     * Helper method that creates Dummy Test Double for the Role Entity
     *
     * @return Role object
     */
    private Role getRole() {
        return Role.builder()
                .id(1L)
                .name("Name Example")
                .build();
    }
}