package com.jarad.postly.repository;

import com.jarad.postly.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findCommentPageByProfile_Id(Long id, Pageable pageable);

    Page<Comment> findByPost_Id(Long id, Pageable pageable);

    Optional<Comment> findByProfile_IdAndId(Long id, Long id1);
}