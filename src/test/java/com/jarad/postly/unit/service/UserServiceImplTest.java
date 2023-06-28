package com.jarad.postly.unit.service;

import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.repository.RoleRepository;
import com.jarad.postly.repository.UserRepository;
import com.jarad.postly.security.SecurityRole;
import com.jarad.postly.service.UserServiceImpl;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.dto.UserDtoOnlyEmail;
import com.jarad.postly.util.dto.UserDtoOnlyPassword;
import com.jarad.postly.util.exception.EmailTemplateException;
import com.jarad.postly.util.exception.UserAlreadyExistException;
import com.jarad.postly.util.exception.UserNotFoundException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private MimeMessage mimeMessage;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private UserDtoOnlyEmail userDtoOnlyEmail;
    private UserDtoOnlyPassword userDtoOnlyPassword;
    private User user;
    private Role role;


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "postlyEmailAddress", "postlyEmail@Example.com");
        userDto = getUserDto();
        userDtoOnlyEmail = getUserDtoOnlyEmail();
        userDtoOnlyPassword = getUserDtoOnlyPassword();
        user = getUser();
        role = getRole();
    }

    @Test
    void registerNewUserAccount_UserDoesNotExist_RegistersNewUserAccount() {
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        userService.registerNewUserAccount(userDto);

        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(passwordEncoder, times(1)).encode(userDto.getPassword());
        verify(roleRepository, times(1)).save(any(Role.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void registerNewUserAccount_UserExists_ThrowsUserAlreadyExistException() {
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistException.class, () -> userService.registerNewUserAccount(userDto));
        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verifyNoMoreInteractions(passwordEncoder, userRepository, mailSender);
    }

    @Test
    void registerNewUserAccount_UserDtoEmailIsEmpty_ThrowsEmailTemplateException() {
        userDto.setEmail("");
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThrows(EmailTemplateException.class, () -> userService.registerNewUserAccount(userDto));
        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(passwordEncoder, times(1)).encode(userDto.getPassword());
        verify(roleRepository, times(1)).save(any(Role.class));
        verifyNoMoreInteractions(mailSender, userRepository);
    }

    @Test
    void resetPasswordForExistingUser_UserIsPresent_ResetsPasswordForExistingUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        userService.resetPasswordForExistingUser(userDtoOnlyEmail);

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).save(user);
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void resetPasswordForExistingUser_UserIsEmpty_ThrowsUserNotFoundException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.resetPasswordForExistingUser(userDtoOnlyEmail));
        verify(userRepository, times(1)).findByEmail(anyString());
        verifyNoMoreInteractions(userRepository, mailSender);
    }

    @Test
    void verifyNewUser_WhenUserIsPresentAndNotEnabled_ReturnsTrue() {
        when(userRepository.findByVerificationCode(anyString())).thenReturn(Optional.of(user));

        boolean actualResult = userService.verifyNewUser("Verification Code Is Present");

        assertTrue(actualResult, "When User IS present AND NOT enabled returns True");
        verify(userRepository, times(1)).findByVerificationCode(anyString());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void verifyNewUser_WhenUserIsPresentAndIsEnabled_ReturnsFalse() {
        user.setEnabled(true);
        when(userRepository.findByVerificationCode(anyString())).thenReturn(Optional.of(user));

        boolean actualResult = userService.verifyNewUser("Verification Code Is Present");

        assertFalse(actualResult, "When User IS present AND IS enabled returns False");
        verify(userRepository, times(1)).findByVerificationCode(anyString());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void verifyNewUser_WhenVerificationCodeIsEmpty_ReturnsFalse() {
        boolean actualResult = userService.verifyNewUser("");

        assertFalse(actualResult, "When VerificationCode IS Empty returns False");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void verifyNewUser_WhenVerificationCodeIsAbsent_ReturnsFalse() {
        boolean actualResult = userService.verifyNewUser(null);

        assertFalse(actualResult, "When VerificationCode IS Absent returns False");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void verifyNewUser_WhenUserIsEmpty_ThrowsUserNotFoundException() {
        when(userRepository.findByVerificationCode(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.verifyNewUser("Verification Code Is Present"));
        verify(userRepository, times(1)).findByVerificationCode(anyString());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void verifyForgotPassword_WhenUserIsPresentAndIsEnabled_ReturnsTrue() {
        user.setEnabled(true);
        when(userRepository.findByVerificationCode(anyString())).thenReturn(Optional.of(user));

        boolean actualResult = userService.verifyForgotPassword("Verification Code Is Present", userDtoOnlyPassword);

        assertTrue(actualResult, "When User IS present AND IS enabled returns True");
        verify(userRepository, times(1)).findByVerificationCode(anyString());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void verifyForgotPassword_WhenUserIsPresentAndIsNotEnabled_ReturnsFalse() {
        when(userRepository.findByVerificationCode(anyString())).thenReturn(Optional.of(user));

        boolean actualResult = userService.verifyForgotPassword("Verification Code Is Present", userDtoOnlyPassword);

        assertFalse(actualResult, "When User IS present AND IS NOT enabled returns False");
        verify(userRepository, times(1)).findByVerificationCode(anyString());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void verifyForgotPassword_WhenVerificationCodeIsEmpty_ReturnsFalse() {
        boolean actualResult = userService.verifyForgotPassword("", userDtoOnlyPassword);

        assertFalse(actualResult, "When Verification Code IS Empty returns False");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void verifyForgotPassword_WhenVerificationCodeIsAbsent_ReturnsFalse() {
        boolean actualResult = userService.verifyForgotPassword(null, userDtoOnlyPassword);

        assertFalse(actualResult, "When Verification Code IS Absent returns False");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void verifyForgotPassword_WhenUserIsEmpty_ThrowsUserNotFoundException() {
        when(userRepository.findByVerificationCode(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.verifyForgotPassword("Verification Code Is Present", userDtoOnlyPassword));
        verify(userRepository, times(1)).findByVerificationCode(anyString());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getOrCreateRole_RoleIsPresent_ReturnsValidRole() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(role));

        Role actualResult = userService.getOrCreateRole(SecurityRole.ROLE_USER);

        assertThat(actualResult).isNotNull();
        assertEquals(role, actualResult, "Roles should be equal");
    }

    @Test
    void getOrCreateRole_RoleIsEmpty_ReturnsNewSavedRole() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role actualResult = userService.getOrCreateRole(SecurityRole.ROLE_USER);

        assertThat(actualResult).isNotNull();
        verify(roleRepository).save(any(Role.class));
        assertEquals(role, actualResult, "Roles should be equal");
    }

    /**
     * Helper method that creates Dummy Test Double for the User Entity
     *
     * @return User object
     */
    private User getUser() {
        return User.builder()
                .id(1L)
                .email("userEmail@Example.com")
                .password("User password Example")
                .build();
    }

    /**
     * Helper method that creates Dummy Test Double for the UserDto Entity
     *
     * @return UserDto object
     */
    private UserDto getUserDto() {
        UserDto userDto = new UserDto();
        userDto.setEmail("userDtoEmail@Example.com");
        userDto.setPassword("userDto Password Example");
        return userDto;
    }

    /**
     * Helper method that creates Dummy Test Double for the UserDtoOnlyEmail Entity
     *
     * @return UserDtoOnlyEmail object
     */
    private UserDtoOnlyEmail getUserDtoOnlyEmail() {
        UserDtoOnlyEmail userDtoOnlyEmail = new UserDtoOnlyEmail();
        userDtoOnlyEmail.setEmail("userDtoEmail@Example.com");
        return userDtoOnlyEmail;
    }

    /**
     * Helper method that creates Dummy Test Double for the UserDtoOnlyPassword Entity
     *
     * @return UserDtoOnlyPassword object
     */
    private UserDtoOnlyPassword getUserDtoOnlyPassword() {
        UserDtoOnlyPassword userDtoOnlyPassword = new UserDtoOnlyPassword();
        return userDtoOnlyPassword;
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