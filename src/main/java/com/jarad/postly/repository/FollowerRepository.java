package com.jarad.postly.repository;

import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.embeddable.FollowerId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, FollowerId> {

    Page<Follower> findAuthorPageById_FollowerId(Long followerId, Pageable pageable);

    Page<Follower> findFollowerPageById_AuthorId(Long authorId, Pageable pageable);

    void deleteById_AuthorIdAndId_FollowerId(Long authorId, Long followerId);


}