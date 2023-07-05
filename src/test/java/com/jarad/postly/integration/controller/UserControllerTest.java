package com.jarad.postly.integration.controller;


import com.jarad.postly.controller.UserController;
import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.UserService;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.dto.UserDtoOnlyEmail;
import com.jarad.postly.util.dto.UserDtoOnlyPassword;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new UserDetailsImpl(getUser());
        // Set the user in the security context
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    public void testShowVerifyPage_Success() throws Exception {
        String code = "testCode";
        when(userService.verifyNewUser(code)).thenReturn(true);

        mockMvc.perform(get("/users/verify")
                        .param("code", code))
                .andExpect(status().isOk())
                .andExpect(view().name("user/verify-success"));

        verify(userService, times(1)).verifyNewUser(code);
    }

    @Test
    public void testShowVerifyPage_NotVerified() throws Exception {
        String code = "testCode";
        when(userService.verifyNewUser(code)).thenReturn(false);

        mockMvc.perform(get("/users/verify")
                        .param("code", code))
                .andExpect(status().isOk())
                .andExpect(view().name("user/verify-fail"));
    }

    @Test
    public void testShowVerifyNotificationPage() throws Exception {
        mockMvc.perform(get("/users/verify-notification"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/verify-notification"));
    }

    @Test
    public void testShowForgotPasswordVerifyPage() throws Exception {
        String code = "testCode";

        mockMvc.perform(get("/users/forgot-password-verify")
                        .param("code", code))
                .andExpect(status().isOk())
                .andExpect(view().name("user/forgot-password-form"))
                .andExpect(model().attribute("code", code))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void testShowForgotPasswordVerifySuccessPage() throws Exception {
        mockMvc.perform(get("/users/forgot-password-verify-success"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/forgot-password-verify-success"));
    }

    @Test
    public void testShowForgotPasswordVerifyFailPage() throws Exception {
        mockMvc.perform(get("/users/forgot-password-verify-fail"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/forgot-password-verify-fail"));
    }

    @Test
    public void testShowForgotPasswordPage() throws Exception {
        mockMvc.perform(get("/users/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/forgot-password"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void testShowForgotPasswordVerifyNotificationPage() throws Exception {
        mockMvc.perform(get("/users/forgot-password-verify-notification"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/forgot-password-verify-notification"));
    }

    @Test
    public void testShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/users/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/registration"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void testProcessForgotPasswordVerifyPage_Success() throws Exception {
        UserDtoOnlyPassword userDtoOnlyPassword = getUserDtoOnlyPassword();
        when(userService.verifyForgotPassword(anyString(), eq(userDtoOnlyPassword))).thenReturn(true);

        mockMvc.perform(post("/users/forgot-password-verify")
                        .with(csrf())
                        .param("code", "1234")
                        .flashAttr("user", userDtoOnlyPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/forgot-password-verify-success"));

        verify(userService, times(1)).verifyForgotPassword(anyString(), eq(userDtoOnlyPassword));
    }

    @Test
    public void testProcessForgotPasswordVerifyPage_FailedForgotPasswordVerification() throws Exception {
        UserDtoOnlyPassword userDtoOnlyPassword = getUserDtoOnlyPassword();
        when(userService.verifyForgotPassword(anyString(), eq(userDtoOnlyPassword))).thenReturn(false);

        mockMvc.perform(post("/users/forgot-password-verify")
                        .with(csrf())
                        .param("code", "1234")
                        .flashAttr("user", userDtoOnlyPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/forgot-password-verify-fail"));

        verify(userService, times(1)).verifyForgotPassword(anyString(), eq(userDtoOnlyPassword));
    }

    @Test
    public void testProcessForgotPasswordVerifyPage_ValidationErrors() throws Exception {
        UserDtoOnlyPassword userDtoOnlyPassword = getUserDtoOnlyPassword();
        userDtoOnlyPassword.setPassword("");
        userDtoOnlyPassword.setMatchingPassword("");

        mockMvc.perform(post("/users/forgot-password-verify")
                        .with(csrf())
                        .param("code", "1234")
                        .flashAttr("user", userDtoOnlyPassword))
                .andExpect(status().isOk())
                .andExpect(view().name("user/forgot-password-form"));

        verifyNoInteractions(userService);
    }

    @Test
    public void testResetPasswordForUserAccount_Success() throws Exception {
        UserDtoOnlyEmail userDtoOnlyEmail = getUserDtoOnlyEmail();

        mockMvc.perform(post("/users/forgot-password")
                        .with(csrf())
                        .flashAttr("user", userDtoOnlyEmail))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/forgot-password-verify-notification"));

        verify(userService, times(1)).resetPasswordForExistingUser(any(UserDtoOnlyEmail.class));
    }

    @Test
    public void testResetPasswordForUserAccount_ValidationErrors() throws Exception {
        UserDtoOnlyEmail userDtoOnlyEmail = getUserDtoOnlyEmail();
        userDtoOnlyEmail.setEmail("");

        mockMvc.perform(post("/users/forgot-password")
                        .with(csrf())
                        .flashAttr("user", userDtoOnlyEmail))
                .andExpect(status().isOk())
                .andExpect(view().name("user/forgot-password"));

        verifyNoInteractions(userService);
    }

    @Test
    public void testRegisterUserAccount_Success() throws Exception {
        UserDto userDto = getUserDto();

        mockMvc.perform(post("/users/registration")
                        .with(csrf())
                        .flashAttr("user", userDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/verify-notification"));

        verify(userService, times(1)).registerNewUserAccount(any(UserDto.class));
    }

    @Test
    public void testRegisterUserAccount_ValidationErrors() throws Exception {
        UserDto userDto = getUserDto();
        userDto.setMatchingPassword("");

        mockMvc.perform(post("/users/registration")
                        .with(csrf())
                        .flashAttr("user", userDto))
                .andExpect(status().isOk())
                .andExpect(view().name("user/registration"));

        verifyNoInteractions(userService);
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
     * Helper method that creates Dummy Test Doubles for the UserDtoOnlyPassword
     *
     * @return UserDtoOnlyPassword
     */
    private UserDtoOnlyPassword getUserDtoOnlyPassword() {
        UserDtoOnlyPassword userDtoOnlyPassword = new UserDtoOnlyPassword();
        userDtoOnlyPassword.setPassword("testpassword");
        userDtoOnlyPassword.setMatchingPassword("testpassword");

        return userDtoOnlyPassword;
    }

    /**
     * Helper method that creates Dummy Test Doubles for the UserDtoOnlyEmail
     *
     * @return UserDtoOnlyEmail
     */
    private UserDtoOnlyEmail getUserDtoOnlyEmail() {
        UserDtoOnlyEmail userDtoOnlyEmail = new UserDtoOnlyEmail();
        userDtoOnlyEmail.setEmail("test@example.com");

        return userDtoOnlyEmail;
    }

    /**
     * Helper method that creates Dummy Test Double for the UserDto Entity
     *
     * @return UserDto object
     */
    private UserDto getUserDto() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");
        userDto.setPassword("testpassword");
        userDto.setMatchingPassword("testpassword");
        return userDto;
    }
}
