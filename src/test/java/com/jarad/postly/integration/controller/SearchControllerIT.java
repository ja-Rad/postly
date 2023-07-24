package com.jarad.postly.integration.controller;

import com.jarad.postly.controller.SearchController;
import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = SearchController.class)
class SearchControllerIT {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SearchService searchService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new UserDetailsImpl(getUser());
        // Set the user in the security context
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    void testReturnSearchResultPage_ResultPageForPosts() throws Exception {
        String query = "testPostTitle";
        int page = 1;
        Page<Post> postPage = new PageImpl<>(getElevenPosts(), PageRequest.of(0, 10), 2);

        when(searchService.findPaginatedPostsContainingTitleByCreationDateDescending(query, page - 1)).thenReturn(postPage);
        when(searchService.returnListOfPageNumbers(anyInt())).thenReturn(new ArrayList<>());

        this.mockMvc.perform(get("/search")
                        .param("q", query)
                        .param("type", "posts")
                        .param("page", String.valueOf(page)))
                .andExpect(status().isOk())
                .andExpect(view().name("search/index"))
                .andExpect(model().attributeExists("query", "totalPostPages", "postPage"));

        verify(searchService, times(1)).findPaginatedPostsContainingTitleByCreationDateDescending(query, page - 1);
    }

    @Test
    void testReturnSearchResultPage_ResultPageForProfiles() throws Exception {
        String query = "testProfileUsername";
        int page = 1;
        Page<Profile> profilePage = new PageImpl<>(getElevenProfiles(), PageRequest.of(0, 10), 2);

        when(searchService.findPaginatedProfilesContainingUsernameByCreationDateDescending(query, page - 1)).thenReturn(profilePage);
        when(searchService.returnListOfPageNumbers(anyInt())).thenReturn(new ArrayList<>());

        this.mockMvc.perform(get("/search")
                        .param("q", query)
                        .param("type", "profiles")
                        .param("page", String.valueOf(page)))
                .andExpect(status().isOk())
                .andExpect(view().name("search/index"))
                .andExpect(model().attributeExists("query", "totalProfilePages", "profilePage"));

        verify(searchService, times(1)).findPaginatedProfilesContainingUsernameByCreationDateDescending(query, page - 1);
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
                .roles(new HashSet<>(Set.of(Role.builder().name("ROLE_USER").build())))
                .enabled(true)
                .build();
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
                    .profile(Profile.builder().id((long) i).build())
                    .build());
        }

        return posts;
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
}