package com.jarad.postly.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.repository.CommentRepository;
import com.jarad.postly.util.dto.CommentDto;
import com.jarad.postly.util.exception.CommentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Comment returnCommentById(Long id) {
        Optional<Comment> optionalComment = commentRepository.findById(id);
        if (optionalComment.isEmpty()) {
            throw new CommentNotFoundException("Comment with this id: " + id + " doesn`t exist");
        }

        return optionalComment.get();
    }

    @Override
    public void updateExistingComment(Long profileId, Long commentId, CommentDto commentDto) {
        Optional<Comment> optionalComment = commentRepository.findByProfile_IdAndId(profileId, commentId);
        if (optionalComment.isEmpty()) {
            throw new CommentNotFoundException("Comment with id: " + commentId + " for profile with id: " + profileId + " doesn`t exist");
        }

        Comment comment = optionalComment.get();
        comment.setDescription(commentDto.getDescription());
        Comment savedComment = commentRepository.save(comment);
    }

    @Override
    public void deleteExistingComment(Long profileId, Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findByProfile_IdAndId(profileId, commentId);
        if (optionalComment.isEmpty()) {
            throw new CommentNotFoundException("Comment with id: " + commentId + " for profile with id: " + profileId + " doesn`t exist");
        }

        Comment comment = optionalComment.get();

        commentRepository.delete(comment);
    }
}

