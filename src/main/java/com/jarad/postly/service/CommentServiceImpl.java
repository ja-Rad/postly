package com.jarad.postly.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.repository.CommentRepository;
import com.jarad.postly.repository.PostRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.util.dto.CommentDto;
import com.jarad.postly.util.exception.CommentNotFoundException;
import com.jarad.postly.util.exception.PostNotFoundException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

@Service
@Transactional(readOnly = true)
@Slf4j
public class CommentServiceImpl implements CommentService {
    public static final int PAGE_SIZE = 10;
    private final CommentRepository commentRepository;
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, ProfileRepository profileRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.profileRepository = profileRepository;
        this.postRepository = postRepository;
    }

    @Override
    public List<Integer> returnListOfPageNumbers(int totalPages) {
        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
    }

    @Override
    public Page<Comment> returnPaginatedCommentsByCreationDateDescending(Long postId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return commentRepository.findByPostId(postId, pageable);
    }

    @Override
    public Set<Long> returnAuthorsByUserId(Long userId) {
        Optional<Profile> optionalProfile = profileRepository.findByUserId(userId);

        if (optionalProfile.isPresent()) {
            Profile profile = optionalProfile.get();

            return profile.getFollowers().stream()
                    .map(follower -> follower.getId().getAuthorId())
                    .collect(toSet());
        }

        return Collections.emptySet();
    }

    @Override
    public Comment returnCommentById(Long postId, Long commentId) {

        Optional<Comment> optionalComment = commentRepository.findByPostIdAndId(postId, commentId);
        if (optionalComment.isEmpty()) {
            String message = MessageFormat.format("Comment with ID {0} doesn''t exist", commentId);
            log.info(message);
            throw new CommentNotFoundException(message);
        }

        return optionalComment.get();
    }

    @Transactional
    @Override
    public Long createNewCommentAndReturnCommentId(Long userId, Long postId, CommentDto commentDto) {
        log.info("Creating a new comment for user with ID {} on post with ID {}", userId, postId);

        Optional<Profile> optionalProfile = profileRepository.findByUserId(userId);
        if (optionalProfile.isEmpty()) {
            String message = MessageFormat.format("Profile with ID {0} doesn''t exist", userId);
            log.info(message);
            throw new ProfileNotFoundException(message);
        }

        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            String message = MessageFormat.format("Post with ID {0} doesn''t exist", postId);
            log.info(message);
            throw new PostNotFoundException(message);
        }

        Comment comment = Comment.builder()
                .description(commentDto.getDescription())
                .creationDate(Instant.now())
                .profile(optionalProfile.get())
                .post(optionalPost.get())
                .build();
        Comment savedComment = commentRepository.save(comment);

        log.info("New comment created with ID {} for user with ID {} on post with ID {}", savedComment.getId(), userId, postId);

        return savedComment.getId();
    }

    @Transactional
    @Override
    public void updateExistingComment(Long userId, Long postId, Long commentId, CommentDto commentDto) {
        log.info("Updating comment with ID {} in post with ID {} for user with ID {}", commentId, postId, userId);

        Optional<Comment> optionalComment = commentRepository.findByProfileUserIdAndPostIdAndId(userId, postId, commentId);
        if (optionalComment.isEmpty()) {
            String message = MessageFormat.format("Comment with ID {0} in post with ID {1} for user with ID {2} doesn''t exist", commentId, postId, userId);
            log.info(message);
            throw new CommentNotFoundException(message);
        }

        Comment comment = optionalComment.get();
        comment.setDescription(commentDto.getDescription());
        commentRepository.save(comment);

        log.info("Comment with ID {} in post with ID {} for user with ID {} has been updated", commentId, postId, userId);
    }

    @Transactional
    @Override
    public void deleteExistingComment(Long userId, Long postId, Long commentId) {
        log.info("Deleting comment with ID {} in post with ID {} for profile with ID {}", commentId, postId, userId);

        Optional<Comment> optionalComment = commentRepository.findByProfileUserIdAndPostIdAndId(userId, postId, commentId);
        if (optionalComment.isEmpty()) {
            String message = MessageFormat.format("Comment with ID {0} in post with ID {1} for user with ID {2} doesn''t exist", commentId, postId, userId);
            log.info(message);
            throw new CommentNotFoundException(message);
        }

        Comment comment = optionalComment.get();

        commentRepository.delete(comment);

        log.info("Comment with ID {} in post with ID {} for user with ID {} has been deleted", commentId, postId, userId);
    }

    @Override
    public boolean isCommentOwnedByUser(Long userId, Long commentId) {
        return commentRepository.existsByProfileUserIdAndId(userId, commentId);
    }
}

