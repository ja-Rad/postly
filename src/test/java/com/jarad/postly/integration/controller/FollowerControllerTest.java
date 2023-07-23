package com.jarad.postly.integration.controller;

import com.jarad.postly.controller.FollowerController;
import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.FollowerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FollowerController.class)
public class FollowerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FollowerService followerService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new UserDetailsImpl(getUser());
        // Set the user in the security context
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    public void testAddFollower() throws Exception {
        Long authorId = 1L;
        String refererUrl = "http://localhost:8080/previousPage";

        mockMvc.perform(post("/followers/{authorId}", authorId)
                        .with(csrf())
                        .with(user(userDetails))
                        .header(HttpHeaders.REFERER, refererUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/previousPage#" + authorId));
    }

    @Test
    public void testDeleteFollower() throws Exception {
        Long authorId = 1L;
        String refererUrl = "http://localhost:8080/previousPage";

        mockMvc.perform(delete("/followers/{authorId}", authorId)
                        .with(csrf())
                        .with(user(userDetails))
                        .header(HttpHeaders.REFERER, refererUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/previousPage#" + authorId));
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
}
