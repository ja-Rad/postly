package com.jarad.postly.repository;

import com.jarad.postly.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findPostPageByProfile_Id(Long id, Pageable pageable);

    Optional<Post> findByProfile_IdAndId(Long profileId, Long postId);

    Optional<Post> findByProfile_User_IdAndId(Long userId, Long postId);

    boolean existsByProfile_User_IdAndId(Long userId, Long postId);

}