package com.jarad.postly.service;

import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.ProfileFollower;
import com.jarad.postly.util.dto.ProfileDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProfileService {
    Page<Profile> returnPaginatedProfilesByCreationDateDescending(int page, int size);

    List<Integer> returnListOfPageNumbers(int totalPages);

    Profile returnProfileById(Long id);

    ProfileFollower returnAuthorsByProfileId(Long id);

    Long createNewProfileAndReturnProfileId(Long id, ProfileDto profileDto);

    void updateExistingProfile(Long id, ProfileDto profileDto);

    void deleteExistingProfile(Long id);

    boolean isProfileExistForUser(Long id);
}
