package com.jarad.postly.service;

import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SearchService {
    List<Integer> returnListOfPageNumbers(int totalPages);

    Page<Post> findPaginatedPostsContainingTitleByCreationDateDescending(String title, int page);

    Page<Profile> findPaginatedProfilesContainingUsernameByCreationDateDescending(String username, int page);
}
