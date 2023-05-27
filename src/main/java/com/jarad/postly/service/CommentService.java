package com.jarad.postly.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.util.dto.CommentDto;

public interface CommentService {

    Comment returnCommentById(Long commentId);

    void updateExistingComment(Long userId, Long commentId, CommentDto commentDto);

    void deleteExistingComment(Long profileId, Long commentId);

    boolean isCommentOwnedByUser(Long profileId, Long commentId);
}
