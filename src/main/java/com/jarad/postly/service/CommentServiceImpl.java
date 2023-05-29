package com.jarad.postly.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.repository.CommentRepository;
import com.jarad.postly.util.dto.CommentDto;
import com.jarad.postly.util.exception.CommentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Comment returnCommentById(Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isEmpty()) {
            String message = "Comment with ID " + commentId + " does not exist";
            log.info(message);
            throw new CommentNotFoundException(message);
        }

        return optionalComment.get();
    }

    @Transactional
    @Override
    public void updateExistingComment(Long userId, Long commentId, CommentDto commentDto) {
        log.info("Updating comment with ID {} for profile with ID {}", commentId, userId);

        Optional<Comment> optionalComment = commentRepository.findByProfile_User_IdAndId(userId, commentId);
        if (optionalComment.isEmpty()) {
            String message = "Comment with id: " + commentId + " for profile with id: " + userId + " doesn`t exist";
            log.info(message);
            throw new CommentNotFoundException(message);
        }

        Comment comment = optionalComment.get();
        comment.setDescription(commentDto.getDescription());
        commentRepository.save(comment);

        log.info("Comment with ID {} for profile with ID {} has been updated", commentId, userId);
    }

    @Transactional
    @Override
    public void deleteExistingComment(Long profileId, Long commentId) {
        log.info("Deleting comment with ID {} for profile with ID {}", commentId, profileId);

        Optional<Comment> optionalComment = commentRepository.findByProfile_User_IdAndId(profileId, commentId);
        if (optionalComment.isEmpty()) {
            String message = "Comment with id: " + commentId + " for profile with id: " + profileId + " doesn`t exist";
            log.info(message);
            throw new CommentNotFoundException(message);
        }

        Comment comment = optionalComment.get();

        commentRepository.delete(comment);

        log.info("Comment with ID {} for profile with ID {} has been deleted", commentId, profileId);
    }

    @Override
    public boolean isCommentOwnedByUser(Long userId, Long commentId) {
        return commentRepository.existsByProfile_User_IdAndId(userId, commentId);
    }
}

