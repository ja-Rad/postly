package com.jarad.postly.repository;

import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Page<Profile> findByUsernameContainsIgnoreCase(String username, Pageable pageable);

    Optional<Profile> findByUserId(Long userId);

    Optional<Profile> findByUserIdAndId(Long userId, Long profileId);

    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndId(Long userId, Long profileId);

    void deleteByUserAndId(User user, Long profileId);
}