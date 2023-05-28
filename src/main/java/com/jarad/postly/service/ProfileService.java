package com.jarad.postly.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.util.dto.ProfileDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface ProfileService {
    Page<Profile> returnPaginatedProfilesByCreationDateDescending(int page);

    Page<Post> returnProfilePaginatedPostsByCreationDateDescending(Long profileId, int page);

    Page<Follower> returnProfilePaginatedAuthorsByCreationDateDescending(Long profileId, int page);

    Page<Follower> returnProfilePaginatedFollowersByCreationDateDescending(Long profileId, int page);

    Page<Comment> returnProfilePaginatedCommentsByCreationDateDescending(Long profileId, int page);

    List<Integer> returnListOfPageNumbers(int totalPages);

    boolean isUserOwnsThisProfile(Long userId, Long profileId);

    Set<Long> returnAuthorsByUserId(Long userId);

    Profile returnProfileById(Long profileId);

    Long createNewProfileAndReturnProfileId(Long userId, ProfileDto profileDto);

    Long updateExistingProfile(Long profileId, ProfileDto profileDto);

    void deleteExistingProfile(Long userId, Long profileId);

    boolean isProfileExistForUser(Long userId);
}
