package com.jarad.postly.repository;

import com.jarad.postly.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser_Id(Long id);

    boolean existsByUser_Id(Long id);

    Set<Profile> findByFollowers_FollowerId(Profile followerId);
}