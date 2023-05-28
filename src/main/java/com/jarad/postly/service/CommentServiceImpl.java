package com.jarad.postly.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.repository.CommentRepository;
import com.jarad.postly.util.dto.CommentDto;
import com.jarad.postly.util.exception.CommentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
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
            throw new CommentNotFoundException("Comment with this id: " + commentId + " doesn`t exist");
        }

        return optionalComment.get();
    }

    @Transactional
    @Override
    public void updateExistingComment(Long userId, Long commentId, CommentDto commentDto) {
        Optional<Comment> optionalComment = commentRepository.findByProfile_User_IdAndId(userId, commentId);
        if (optionalComment.isEmpty()) {
            throw new CommentNotFoundException("Comment with id: " + commentId + " for profile with id: " + userId + " doesn`t exist");
        }

        Comment comment = optionalComment.get();
        comment.setDescription(commentDto.getDescription());
        commentRepository.save(comment);
    }

    @Transactional
    @Override
    public void deleteExistingComment(Long profileId, Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findByProfile_User_IdAndId(profileId, commentId);
        if (optionalComment.isEmpty()) {
            throw new CommentNotFoundException("Comment with id: " + commentId + " for profile with id: " + profileId + " doesn`t exist");
        }

        Comment comment = optionalComment.get();

        commentRepository.delete(comment);
    }

    @Override
    public boolean isCommentOwnedByUser(Long userId, Long commentId) {
        return commentRepository.existsByProfile_User_IdAndId(userId, commentId);
    }
}

