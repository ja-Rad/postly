package com.jarad.postly.repository;

import com.jarad.postly.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findCommentPageByProfileId(Long profileId, Pageable pageable);

    Page<Comment> findByPostId(Long postId, Pageable pageable);

    Optional<Comment> findByProfileUserIdAndId(Long userId, Long commentId);

    Optional<Comment> findByProfileUserIdAndPostIdAndId(Long userId, Long postId, Long commentId);

    Optional<Comment> findByPostIdAndId(Long postId, Long commentId);

    boolean existsByProfileUserIdAndId(Long userId, Long commentId);
}