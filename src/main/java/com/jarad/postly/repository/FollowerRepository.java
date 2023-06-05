package com.jarad.postly.repository;

import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.embeddable.FollowerId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, FollowerId> {

    Page<Follower> findAuthorPageByIdFollowerId(Long followerId, Pageable pageable);

    Page<Follower> findFollowerPageByIdAuthorId(Long authorId, Pageable pageable);

    void deleteByIdAuthorIdAndIdFollowerId(Long authorId, Long followerId);


}