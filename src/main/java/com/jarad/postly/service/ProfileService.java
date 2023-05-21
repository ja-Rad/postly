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
    Page<Profile> returnPaginatedProfilesByCreationDateDescending(int page, int size);

    Page<Post> returnProfilePaginatedPostsByCreationDateDescending(Long id, int page, int size);

    Page<Follower> returnProfilePaginatedAuthorsByCreationDateDescending(Long id, int page, int size);

    Page<Follower> returnProfilePaginatedFollowersByCreationDateDescending(Long id, int page, int size);

    Page<Comment> returnProfilePaginatedCommentsByCreationDateDescending(Long id, int page, int size);

    List<Integer> returnListOfPageNumbers(int totalPages);

    Set<Long> returnAuthorsByUserId(Long userId);

    Profile returnProfileById(Long id);

    Long createNewProfileAndReturnProfileId(Long id, ProfileDto profileDto);

    Long updateExistingProfile(Long id, ProfileDto profileDto);

    void deleteExistingProfile(Long id);

    boolean isProfileExistForUser(Long id);
}
