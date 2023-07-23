package com.jarad.postly.unit.security;

import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.repository.UserRepository;
import com.jarad.postly.security.UserDetailsServiceImpl;
import com.jarad.postly.util.exception.EmailNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_UserIsPresent_ReturnsUserDetails() {
        User user = getUser();
        String userEmail = user.getEmail();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        UserDetails actualResult = userDetailsService.loadUserByUsername(userEmail);

        assertThat(actualResult).isNotNull();
        assertAll("Verify User properties",
                () -> assertEquals(userEmail, actualResult.getUsername()),
                () -> assertEquals(user.getPassword(), actualResult.getPassword())
        );
        verify(userRepository, times(1)).findByEmail(userEmail);
    }

    @Test
    void loadUserByUsername_UserIsEmpty_ThrowsEmailNotFoundException() {
        User user = getUser();
        String userEmail = user.getEmail();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> userDetailsService.loadUserByUsername(userEmail));
        verify(userRepository, times(1)).findByEmail(userEmail);
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
