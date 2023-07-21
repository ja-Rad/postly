package com.jarad.postly.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Post;
import com.jarad.postly.util.dto.CommentDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface CommentService {
    List<Integer> returnListOfPageNumbers(int totalPages);

    Page<Comment> returnPaginatedCommentsByCreationDateDescending(Long postId, int page);

    Set<Long> returnAuthorsByUserId(Long userId);

    Comment returnCommentById(Long postId, Long commentId);

    Post returnPostByPostId(Long postId);

    Long createNewCommentAndReturnCommentId(Long userId, Long postId, CommentDto commentDto);

    void updateExistingComment(Long userId, Long postId, Long commentId, CommentDto commentDto);

    void deleteExistingComment(Long userId, Long postId, Long commentId);

    boolean isCommentOwnedByUser(Long userId, Long commentId);
}
