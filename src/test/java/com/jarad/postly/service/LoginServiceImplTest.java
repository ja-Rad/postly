package com.jarad.postly.service;

import com.jarad.postly.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;
    @InjectMocks
    private LoginServiceImpl loginService;

    @Test
    void isProfileExistForUser_WhenProfileExistsForUser_ReturnsTrue() {
        when(profileRepository.existsByUserId(anyLong())).thenReturn(true);

        boolean actualResult = loginService.isProfileExistForUser(1L);

        assertTrue(actualResult, "When Profile EXISTS for User");
        verify(profileRepository, times(1)).existsByUserId(anyLong());
    }

    @Test
    void isProfileExistForUser_WhenProfileDoesNotExistForUser_ReturnsFalse() {
        when(profileRepository.existsByUserId(anyLong())).thenReturn(false);

        boolean actualResult = loginService.isProfileExistForUser(1L);

        assertFalse(actualResult, "When Profile DOES NOT EXIST for User");
        verify(profileRepository, times(1)).existsByUserId(anyLong());
    }
}