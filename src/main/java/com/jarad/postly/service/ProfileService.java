package com.jarad.postly.service;

import com.jarad.postly.entity.Profile;
import com.jarad.postly.util.dto.ProfileDto;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProfileService {
    Page<Profile> returnPaginatedProfilesByCreationDateDescending(int page, int size);

    List<Integer> returnListOfPageNumbers(int totalPages);

    Profile returnProfileById(long id);

    Long createNewProfileAndReturnProfileId(Authentication authentication, ProfileDto profileDto);

    Long updateExistingProfileAndReturnProfileId(Authentication authentication, ProfileDto profileDto);

    void deleteExistingProfile(Authentication authentication);

    boolean isProfileExistForUser(Authentication authentication);
}
