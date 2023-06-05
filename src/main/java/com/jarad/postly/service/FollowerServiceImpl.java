package com.jarad.postly.service;

import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.embeddable.FollowerPK;
import com.jarad.postly.repository.FollowerRepository;
import com.jarad.postly.repository.ProfileRepository;
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

        findProfileById(authorId, "Authors");
        findProfileById(followerId, "Followers");

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

        findProfileById(authorId, "Authors");
        findProfileById(followerId, "Followers");

        followerRepository.deleteByIdAuthorIdAndIdFollowerId(authorId, followerId);

        log.info("Follower with ID {} has been deleted from author with ID {}", followerId, authorId);
    }

    /**
     * Helper Method to find Profile based on its type
     *
     * @param profileType is represented by Authors and Followers
     */
    private void findProfileById(Long profileId, String profileType) {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) {
            String message = MessageFormat.format("{0} Profile with ID {1} doesn''t exist", profileType, profileId);
            log.info(message);
            throw new ProfileNotFoundException(message);
        }
    }
}
