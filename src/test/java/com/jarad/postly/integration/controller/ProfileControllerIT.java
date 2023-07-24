package com.jarad.postly.integration.controller;

import com.jarad.postly.controller.ProfileController;
import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.entity.embeddable.FollowerPK;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.ProfileService;
import com.jarad.postly.util.dto.ProfileDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = ProfileController.class)
class ProfileControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProfileService profileService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new UserDetailsImpl(getUser());
        // Set the user in the security context
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    void testGetPaginatedProfiles() throws Exception {
        Page<Profile> profilePage = new PageImpl<>(getElevenProfiles(), PageRequest.of(0, 10), 2);
        when(profileService.returnAuthorsByUserId(anyLong())).thenReturn(new HashSet<>());
        when(profileService.returnPaginatedProfilesByCreationDateDescending(anyInt())).thenReturn(profilePage);
        when(profileService.returnListOfPageNumbers(anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/profiles")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profiles"))
                .andExpect(model().attribute("authorsByUserId", new HashSet<>()))
                .andExpect(model().attribute("profilePage", profilePage))
                .andExpect(model().attribute("userId", userDetails.getUserId()))
                .andExpect(model().attribute("activeProfile", userDetails.isActiveProfile()));

        verify(profileService, times(1)).returnAuthorsByUserId(anyLong());
        verify(profileService, times(1)).returnPaginatedProfilesByCreationDateDescending(anyInt());
        verify(profileService, times(1)).returnListOfPageNumbers(anyInt());
    }

    @Test
    void testGetProfileById() throws Exception {
        Profile profile = getProfile();
        Long profileId = profile.getId();
        when(profileService.returnAuthorsByUserId(anyLong())).thenReturn((new HashSet<>()));
        when(profileService.returnProfileById(profileId)).thenReturn(profile);
        when(profileService.isUserOwnsThisProfile(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(get("/profiles/" + profileId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("authorsByUserId", (new HashSet<>())))
                .andExpect(model().attribute("profile", profile))
                .andExpect(model().attribute("userId", userDetails.getUserId()))
                .andExpect(model().attribute("activeProfile", userDetails.isActiveProfile()))
                .andExpect(view().name("profile/profile"));

        verify(profileService, times(1)).returnAuthorsByUserId(anyLong());
        verify(profileService, times(1)).returnProfileById(profileId);
        verify(profileService, times(1)).isUserOwnsThisProfile(anyLong(), anyLong());
    }

    @Test
    void testGetProfilePaginatedPosts() throws Exception {
        Long profileId = 1L;
        Page<Post> postPage = new PageImpl<>(getElevenPosts(), PageRequest.of(0, 10), 2);
        when(profileService.isUserOwnsThisProfile(anyLong(), anyLong())).thenReturn(true);
        when(profileService.returnProfilePaginatedPostsByCreationDateDescending(eq(profileId), anyInt())).thenReturn(postPage);
        when(profileService.returnListOfPageNumbers(anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/profiles/" + profileId + "/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("profileId", profileId))
                .andExpect(model().attribute("postPage", postPage))
                .andExpect(model().attribute("pageNumbers", new ArrayList<>()))
                .andExpect(view().name("profile/profile-posts"));

        verify(profileService, times(1)).returnProfilePaginatedPostsByCreationDateDescending(profileId, 0);
        verify(profileService, times(1)).returnListOfPageNumbers(anyInt());
    }

    @Test
    void testGetProfilePaginatedAuthors() throws Exception {
        Long profileId = 1L;
        Page<Follower> authorPage = new PageImpl<>(getElevenAuthors(profileId), PageRequest.of(0, 10), 2);
        when(profileService.isUserOwnsThisProfile(anyLong(), anyLong())).thenReturn(true);
        when(profileService.returnProfilePaginatedAuthorsByCreationDateDescending(eq(profileId), anyInt())).thenReturn(authorPage);
        when(profileService.returnListOfPageNumbers(anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/profiles/" + profileId + "/authors"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("profileId", profileId))
                .andExpect(model().attribute("authorPage", authorPage))
                .andExpect(model().attribute("pageNumbers", new ArrayList<>()))
                .andExpect(view().name("profile/profile-authors"));

        verify(profileService, times(1)).returnProfilePaginatedAuthorsByCreationDateDescending(profileId, 0);
        verify(profileService, times(1)).returnListOfPageNumbers(anyInt());
    }

    @Test
    void testGetProfilePaginatedFollowers() throws Exception {
        Long profileId = 1L;
        Page<Follower> followerPage = new PageImpl<>(getElevenFollowers(profileId), PageRequest.of(0, 10), 2);
        when(profileService.isUserOwnsThisProfile(anyLong(), anyLong())).thenReturn(true);
        when(profileService.returnProfilePaginatedFollowersByCreationDateDescending(eq(profileId), anyInt())).thenReturn(followerPage);
        when(profileService.returnListOfPageNumbers(anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/profiles/" + profileId + "/followers"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("profileId", profileId))
                .andExpect(model().attribute("followerPage", followerPage))
                .andExpect(model().attribute("pageNumbers", new ArrayList<>()))
                .andExpect(view().name("profile/profile-followers"));

        verify(profileService, times(1)).returnProfilePaginatedFollowersByCreationDateDescending(profileId, 0);
        verify(profileService, times(1)).returnListOfPageNumbers(anyInt());
    }

    @Test
    void testGetProfilePaginatedComments() throws Exception {
        Long profileId = 1L;
        Page<Comment> commentPage = new PageImpl<>(getElevenComments(), PageRequest.of(0, 10), 2);
        when(profileService.isUserOwnsThisProfile(anyLong(), anyLong())).thenReturn(true);
        when(profileService.returnProfilePaginatedCommentsByCreationDateDescending(eq(profileId), anyInt())).thenReturn(commentPage);
        when(profileService.returnListOfPageNumbers(anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/profiles/" + profileId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("profileId", profileId))
                .andExpect(model().attribute("commentPage", commentPage))
                .andExpect(model().attribute("pageNumbers", new ArrayList<>()))
                .andExpect(view().name("profile/profile-comments"));

        verify(profileService, times(1)).returnProfilePaginatedCommentsByCreationDateDescending(profileId, 0);
        verify(profileService, times(1)).returnListOfPageNumbers(anyInt());
    }

    @Test
    void testGetProfileForm_Success() throws Exception {
        when(profileService.isProfileExistForUser(anyLong())).thenReturn(true);

        mockMvc.perform(get("/profiles/create-form"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles"));

        verify(profileService, times(1)).isProfileExistForUser(anyLong());
    }

    @Test
    void testGetProfileForm_ProfileNotExists() throws Exception {
        when(profileService.isProfileExistForUser(anyLong())).thenReturn(false);

        mockMvc.perform(get("/profiles/create-form"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile-create-form"));

        verify(profileService, times(1)).isProfileExistForUser(anyLong());
    }

    @Test
    void testGetProfileUpdateForm() throws Exception {
        Profile profile = getProfile();
        Long profileId = profile.getId();
        when(profileService.returnProfileById(profileId)).thenReturn(profile);

        mockMvc.perform(get("/profiles/" + profileId + "/update-form"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("profile", profile))
                .andExpect(view().name("profile/profile-update-form"));

        verify(profileService, times(1)).returnProfileById(profileId);
    }

    @Test
    void testCreateNewProfile_Success() throws Exception {
        ProfileDto profileDto = getProfileDto();
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/profiles")
                        .with(csrf())
                        .flashAttr("profile", profileDto)
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        assertTrue(session.isInvalid());
        verify(profileService, times(1)).createNewProfile(anyLong(), eq(profileDto));
    }

    @Test
    void testCreateNewProfile_ValidationErrors() throws Exception {
        ProfileDto profileDto = getProfileDto();
        profileDto.setUsername("");

        mockMvc.perform(post("/profiles")
                        .with(csrf())
                        .flashAttr("profile", profileDto))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile-create-form"));
    }

    @Test
    void testUpdateExistingProfile_Success() throws Exception {
        Long profileId = 1L;
        ProfileDto profileDto = getProfileDto();

        mockMvc.perform(put("/profiles/" + profileId)
                        .with(csrf())
                        .flashAttr("profile", profileDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles/" + profileId));

        verify(profileService, times(1)).updateExistingProfile(anyLong(), eq(profileId), eq(profileDto));
    }

    @Test
    void testUpdateExistingProfile_ValidationErrors() throws Exception {
        Long profileId = 1L;
        ProfileDto profileDto = getProfileDto();
        profileDto.setUsername("");

        mockMvc.perform(put("/profiles/" + profileId)
                        .with(csrf())
                        .flashAttr("profile", profileDto))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile-update-form"));
    }

    @Test
    void testDeleteExistingProfile() throws Exception {
        Long profileId = 1L;
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(delete("/profiles/" + profileId).session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        assertTrue(session.isInvalid());
        verify(profileService, times(1)).deleteExistingProfile(anyLong(), eq(profileId));
    }

    /**
     * Helper method that creates Dummy Test Doubles for the User
     *
     * @return User
     */
    private User getUser() {
        return User.builder()
                .id(1L)
                .email("1stUserEmail@Example.com")
                .password("1st User Password Example")
                .roles(new HashSet<>(Set.of(Role.builder().name("ROLE_PROFILE_ACTIVE").build())))
                .enabled(true)
                .build();
    }

    /**
     * Helper method that creates Dummy Test Doubles for the List of Profiles
     *
     * @return List of Profiles
     */
    private List<Profile> getElevenProfiles() {
        List<Profile> profiles = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            profiles.add(Profile.builder()
                    .id((long) i)
                    .username(i + " Profile Username Example")
                    .creationDate(Instant.now())
                    .user(User.builder()
                            .id((long) i)
                            .build()
                    )
                    .comments(List.of(Comment.builder().build()))
                    .posts(List.of(Post.builder().build()))
                    .build());
        }

        return profiles;
    }

    /**
     * Helper method that creates Dummy Test Doubles for the List of Posts
     *
     * @return List of Posts
     */
    private List<Post> getElevenPosts() {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            posts.add(Post.builder()
                    .id((long) i)
                    .title((long) i + "Post Title Example")
                    .description((long) i + "Post Description Example")
                    .creationDate(Instant.now())
                    .comments(List.of(Comment.builder().build()))
                    .build());
        }

        return posts;
    }

    /**
     * Helper method that creates Dummy Test Doubles for the List of Followers
     *
     * @return List of Followers
     */
    private List<Follower> getElevenAuthors(Long followerId) {
        List<Follower> authors = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            authors.add(Follower.builder()
                    .id(FollowerPK.builder().followerId(followerId).authorId((long) i).build())
                    .creationDate(Instant.now())
                    .profileAuthor(Profile.builder().username((long) i + "Profile Username Example").build())
                    .build());
        }
        return authors;
    }

    /**
     * Helper method that creates Dummy Test Doubles for the List of Followers
     *
     * @return List of Followers
     */
    private List<Follower> getElevenFollowers(Long authorId) {
        List<Follower> followers = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            followers.add(Follower.builder()
                    .id(FollowerPK.builder().followerId((long) i).authorId(authorId).build())
                    .creationDate(Instant.now())
                    .profileFollower(Profile.builder().username((long) i + "Profile Username Example").build())
                    .build());
        }
        return followers;
    }

    /**
     * Helper method that creates Dummy Test Doubles for the List of Comments
     *
     * @return List of Comments
     */
    private List<Comment> getElevenComments() {
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            comments.add(Comment.builder()
                    .id((long) i)
                    .description((long) i + "Comment Description Example")
                    .creationDate(Instant.now())
                    .post(Post.builder()
                            .id((long) i)
                            .title("Post Title Example")
                            .description("Post Description Example")
                            .creationDate(Instant.now())
                            .build())
                    .build());
        }

        return comments;
    }

    /**
     * Helper method that creates Dummy Test Doubles for the Profile
     *
     * @return Profile
     */
    private Profile getProfile() {
        return Profile.builder()
                .id(1L)
                .username("Profile Username Example")
                .creationDate(Instant.now())
                .user(User.builder().id(1L).build())
                .authors(List.of(Follower.builder().profileAuthor(Profile.builder().build()).build()))
                .posts(List.of(Post.builder().build()))
                .comments(List.of(Comment.builder().build()))
                .build();
    }

    /**
     * Helper method that creates Dummy Test Doubles for the ProfileDto
     *
     * @return ProfileDto
     */
    private ProfileDto getProfileDto() {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setUsername("ProfileDto Username Example");

        return profileDto;
    }
}
