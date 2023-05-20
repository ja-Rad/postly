package com.jarad.postly.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Post;
import com.jarad.postly.util.dto.CommentDto;
import com.jarad.postly.util.dto.PostDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface PostService {
    List<Integer> returnListOfPageNumbers(int totalPages);

    Page<Post> returnPaginatedPostsByCreationDateDescending(int page, int size);

    Page<Comment> returnPaginatedCommentsByCreationDateDescending(Long id, int page, int size);

    Set<Long> returnAuthorsByUserId(Long userId);

    Post returnPostById(Long id);

    Long createNewPostAndReturnPostId(Long id, PostDto postDto);

    Long updateExistingPost(Long profileId, Long postId, PostDto postDto);

    Long createNewCommentAndReturnCommentId(Long postId, Long id, CommentDto commentDto);

    void deleteExistingPost(Long profileId, Long postId);
}
