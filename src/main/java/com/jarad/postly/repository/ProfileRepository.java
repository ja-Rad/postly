package com.jarad.postly.repository;

import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    void deleteByUserAndId(User user, Long profileId);
}