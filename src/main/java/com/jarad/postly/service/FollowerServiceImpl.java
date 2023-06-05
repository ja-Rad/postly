package com.jarad.postly.service;

import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.embeddable.FollowerPK;
import com.jarad.postly.repository.FollowerRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.util.exception.AuthorNotFoundException;
import com.jarad.postly.util.exception.FollowerServiceException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
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
            String message = MessageFormat.format("Authors Profile with ID {0} doesn''t exist", authorId);
            log.info(message);
            throw new AuthorNotFoundException(message);
        }

        Optional<Profile> optionalFollowersProfile = profileRepository.findById(followerId);
        if (optionalFollowersProfile.isEmpty()) {
            String message = MessageFormat.format("Followers Profile with ID {0} doesn''t exist", followerId);
            log.info(message);
            throw new ProfileNotFoundException(message);
        }

        if (Objects.equals(followerId, authorId)) {
            String message = MessageFormat.format("Follower with ID {0} can''t follow author with same ID {1} because Self-following is not allowed", followerId, authorId);
            log.info(message);
            throw new FollowerServiceException(message);
        }

        FollowerPK followerPrimaryKey = FollowerPK.builder()
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
            String message = MessageFormat.format("Authors Profile with ID {0} doesn''t exist", authorId);
            log.info(message);
            throw new AuthorNotFoundException(message);
        }

        Optional<Profile> optionalFollowersProfile = profileRepository.findById(followerId);
        if (optionalFollowersProfile.isEmpty()) {
            String message = MessageFormat.format("Followers Profile with ID {0} doesn''t exist", followerId);
            log.info(message);
            throw new ProfileNotFoundException(message);
        }

        followerRepository.deleteByIdAuthorIdAndIdFollowerId(authorId, followerId);

        log.info("Follower with ID {} has been deleted from author with ID {}", followerId, authorId);
    }
}
