package com.jarad.postly.service;

import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.repository.PostRepository;
import com.jarad.postly.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
@Slf4j
public class SearchServiceImpl implements SearchService {

    public static final int PAGE_SIZE = 10;
    private PostRepository postRepository;
    private ProfileRepository profileRepository;

    @Autowired
    public SearchServiceImpl(PostRepository postRepository, ProfileRepository profileRepository) {
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public List<Integer> returnListOfPageNumbers(int totalPages) {
        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
    }

    @Override
    public Page<Post> findPaginatedPostsContainingTitleByCreationDateDescending(String title, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return postRepository.findByTitleContainsIgnoreCase(title, pageable);
    }

    @Override
    public Page<Profile> findPaginatedProfilesContainingUsernameByCreationDateDescending(String username, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return profileRepository.findByUsernameContainsIgnoreCase(username, pageable);
    }
}
