package com.jarad.postly.service;

import com.jarad.postly.entity.Post;
import com.jarad.postly.util.dto.PostDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface PostService {
    List<Integer> returnListOfPageNumbers(int totalPages);

    Page<Post> returnPaginatedPostsByCreationDateDescending(int page);

    Set<Long> returnAuthorsByUserId(Long userId);

    Post returnPostById(Long postId);

    Long createNewPostAndReturnPostId(Long userId, PostDto postDto);

    void updateExistingPost(Long profileId, Long postId, PostDto postDto);

    void deleteExistingPost(Long profileId, Long postId);

    boolean isPostOwnedByUser(Long userId, Long postId);

    String returnTitleByPostId(Long postId);
}
