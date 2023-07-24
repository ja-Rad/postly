package com.jarad.postly.integration.config;

import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIT {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserDetailsService userDetailsService;
    private UserDetailsImpl userDetails;

    private static Stream<String> provideGetUrlForRoleUserWithOkStatus() {
        return Stream.of(
                "/posts", "/posts/1", "/posts/1/comments", "/posts/1/comments/2",

                "/profiles", "/profiles/1", "/profiles/1/posts", "/profiles/1/comments",
                "/profiles/1/authors", "/profiles/1/followers",

                "/search?q=JohnDoe"
        );
    }

    private static Stream<String> provideGetUrlForRoleUserWithRedirectStatus() {
        return Stream.of("/", "/profiles/create-form");
    }

    private static Stream<String> provideGetUrlForRoleProfileActiveWithOkStatus() {
        return Stream.of(
                "/posts/create-form", "/posts/1/update-form",

                "/profiles/1/update-form"
        );
    }

    @ParameterizedTest
    @MethodSource("provideGetUrlForRoleUserWithOkStatus")
    void testRoleUserEndpoints_Ok(String roleUserURL) throws Exception {
        userDetails = new UserDetailsImpl(getUserWithRoleUser());

        mockMvc.perform(get(roleUserURL)
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("provideGetUrlForRoleUserWithRedirectStatus")
    void testRoleUserEndpoints_Redirect(String roleUserURL) throws Exception {
        userDetails = new UserDetailsImpl(getUserWithRoleUser());

        mockMvc.perform(get(roleUserURL)
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @ParameterizedTest
    @MethodSource("provideGetUrlForRoleProfileActiveWithOkStatus")
    void testRoleProfileActiveEndpoints_Ok(String roleUserURL) throws Exception {
        userDetails = new UserDetailsImpl(getUserWithRoleProfileActive());

        mockMvc.perform(get(roleUserURL)
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void testRoleProfileActiveEndpoints_Redirect() throws Exception {
        userDetails = new UserDetailsImpl(getUserWithRoleProfileActive());

        mockMvc.perform(get("/profiles/create-form")
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testAccessToSecuredEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/posts")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void testAccessToAdminEndpointWithUserCredentials() throws Exception {
        mockMvc.perform(get("/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    /**
     * Helper method that creates Dummy Test Doubles for the User With ROLE_USER
     *
     * @return User
     */
    private User getUserWithRoleUser() {
        return User.builder()
                .id(1L)
                .email("1stUserEmail@Example.com")
                .password("1st User Password Example")
                .roles(new HashSet<>(Set.of(
                        Role.builder().name("ROLE_USER").build()
                )))
                .enabled(true)
                .activeProfile(true)
                .build();
    }

    /**
     * Helper method that creates Dummy Test Doubles for the User With ROLE_PROFILE_ACTIVE
     *
     * @return User
     */
    private User getUserWithRoleProfileActive() {
        return User.builder()
                .id(1L)
                .email("1stUserEmail@Example.com")
                .password("1st User Password Example")
                .roles(new HashSet<>(Set.of(
                        Role.builder().name("ROLE_USER").build(),
                        Role.builder().name("ROLE_PROFILE_ACTIVE").build()
                )))
                .enabled(true)
                .activeProfile(true)
                .build();
    }
}