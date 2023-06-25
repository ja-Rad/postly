package com.jarad.postly.service;

import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.embeddable.FollowerPK;
import com.jarad.postly.repository.FollowerRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.util.exception.FollowerServiceException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowerServiceImplTest {

    @Mock
    private FollowerRepository followerRepository;
    @Mock
    private ProfileRepository profileRepository;
    @InjectMocks
    private FollowerServiceImpl followerService;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = getProfile();
    }

    @Test
    void addFollowerToAuthor_ProfileIsPresent_AddsFollowerToAuthor() {
        when(profileRepository.findById(anyLong())).thenReturn(Optional.of(profile));

        followerService.addFollowerToAuthor(1L, 2L);

        verify(profileRepository, times(2)).findById(anyLong());
        verify(followerRepository, times(1)).save(any(Follower.class));
    }

    @Test
    void addFollowerToAuthor_ProfileIsEmpty_ThrowsProfileNotFoundException() {
        when(profileRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> followerService.addFollowerToAuthor(1L, 2L));
        verify(profileRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(followerRepository);
    }

    @Test
    void addFollowerToAuthor_FollowerIdEqualsAuthorId_ThrowsFollowerServiceException() {
        when(profileRepository.findById(anyLong())).thenReturn(Optional.of(profile));

        assertThrows(FollowerServiceException.class, () -> followerService.addFollowerToAuthor(1L, 1L));
        verify(profileRepository, times(2)).findById(anyLong());
        verifyNoMoreInteractions(followerRepository);
    }

    @Test
    void deleteFollowerFromAuthor_ProfileIsPresent_DeletesFollowerFromAuthor() {
        when(profileRepository.findById(anyLong())).thenReturn(Optional.of(profile));

        followerService.deleteFollowerFromAuthor(1L, 2L);

        verify(profileRepository, times(2)).findById(anyLong());
        verify(followerRepository, times(1)).deleteByIdAuthorIdAndIdFollowerId(anyLong(), anyLong());
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

}