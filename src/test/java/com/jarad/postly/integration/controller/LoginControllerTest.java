package com.jarad.postly.integration.controller;

import com.jarad.postly.controller.LoginController;
import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoginController.class)
public class LoginControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LoginService loginService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new UserDetailsImpl(getUser());
        // Set the user in the security context
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    void testShowIndexPage_ProfileExists() throws Exception {
        when(loginService.isProfileExistForUser(1L)).thenReturn(true);

        mockMvc.perform(get("/")
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"))
                .andExpect(request().sessionAttribute("usersActiveProfileId", equalTo(1L)));

        verify(loginService).isProfileExistForUser(1L);
    }

    @Test
    void testShowIndexPage_ProfileNotExists() throws Exception {
        when(loginService.isProfileExistForUser(1L)).thenReturn(false);

        mockMvc.perform(get("/")
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles/create-form"))
                .andExpect(request().sessionAttribute("usersActiveProfileId", nullValue()));

        verify(loginService).isProfileExistForUser(1L);
    }

    @Test
    void testShowLoginPage() throws Exception {
        mockMvc.perform(get("/login")
                        .with(csrf()))
                .andExpect(status().isOk());
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
}
