package com.jarad.postly.service;

import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.embeddable.FollowerId;
import com.jarad.postly.repository.FollowerRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.util.exception.AuthorNotFoundException;
import com.jarad.postly.util.exception.FollowerServiceException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class FollowerServiceImpl implements FollowerService {

    private final FollowerRepository followerRepository;
    private final ProfileRepository profileRepository;

    @Autowired
    public FollowerServiceImpl(FollowerRepository followerRepository, ProfileRepository profileRepository) {
        this.followerRepository = followerRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    @Override
    public void addFollowerToAuthor(Long followerId, Long authorId) {
        log.info("Adding follower with ID {} to author with ID {}", followerId, authorId);

        Optional<Profile> optionalAuthorsProfile = profileRepository.findById(authorId);
        if (optionalAuthorsProfile.isEmpty()) {
            String message = "Authors Profile with id: " + authorId + " doesn`t exist";
            log.info(message);
            throw new AuthorNotFoundException(message);
        }

        Optional<Profile> optionalFollowersProfile = profileRepository.findById(followerId);
        if (optionalFollowersProfile.isEmpty()) {
            String message = "Followers Profile with id: " + followerId + " doesn`t exist";
            log.info(message);
            throw new ProfileNotFoundException(message);
        }

        if (Objects.equals(followerId, authorId)) {
            String message = "Follower with id: " + followerId + " can`t follow author with same id: " + authorId + " because Self-following is not allowed";
            log.info(message);
            throw new FollowerServiceException(message);
        }

        FollowerId followerPrimaryKey = FollowerId.builder()
                .authorId(authorId)
                .followerId(followerId)
                .build();

        Follower follower = Follower.builder()
                .id(followerPrimaryKey)
                .creationDate(Instant.now())
                .build();

        followerRepository.save(follower);

        log.info("Follower with ID {} has been added to author with ID {}", followerId, authorId);
    }

    @Transactional
    @Override
    public void deleteFollowerFromAuthor(Long followerId, Long authorId) {
        log.info("Deleting follower with ID {} from author with ID {}", followerId, authorId);

        Optional<Profile> optionalAuthorsProfile = profileRepository.findById(authorId);
        if (optionalAuthorsProfile.isEmpty()) {
            String message = "Authors Profile with id: " + authorId + " doesn`t exist";
            log.info(message);
            throw new AuthorNotFoundException(message);
        }

        Optional<Profile> optionalFollowersProfile = profileRepository.findById(followerId);
        if (optionalFollowersProfile.isEmpty()) {
            String message = "Followers Profile with id: " + followerId + " doesn`t exist";
            log.info(message);
            throw new ProfileNotFoundException("Followers Profile with id: " + followerId + " doesn`t exist");
        }

        followerRepository.deleteById_AuthorIdAndId_FollowerId(authorId, followerId);

        log.info("Follower with ID {} has been deleted from author with ID {}", followerId, authorId);
    }
}
