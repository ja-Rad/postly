package com.jarad.postly.service;

import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.embeddable.FollowerId;
import com.jarad.postly.repository.FollowerRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.util.exception.AuthorNotFoundException;
import com.jarad.postly.util.exception.FollowerServiceException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
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
        Optional<Profile> optionalAuthorsProfile = profileRepository.findById(authorId);
        if (optionalAuthorsProfile.isEmpty()) {
            throw new AuthorNotFoundException("Authors Profile with id: " + authorId + " doesn`t exist");
        }
        Optional<Profile> optionalFollowersProfile = profileRepository.findById(followerId);
        if (optionalFollowersProfile.isEmpty()) {
            throw new ProfileNotFoundException("Followers Profile with id: " + followerId + " doesn`t exist");
        }
        if (Objects.equals(followerId, authorId)) {
            throw new FollowerServiceException("Follower with id: " + followerId + " can`t follow author with same id: " + authorId + " because Self-following is not allowed");
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
    }

    @Transactional
    @Override
    public void deleteFollowerFromAuthor(Long followerId, Long authorId) {
        Optional<Profile> optionalAuthorsProfile = profileRepository.findById(authorId);
        if (optionalAuthorsProfile.isEmpty()) {
            throw new AuthorNotFoundException("Authors Profile with id: " + authorId + " doesn`t exist");
        }
        Optional<Profile> optionalFollowersProfile = profileRepository.findById(followerId);
        if (optionalFollowersProfile.isEmpty()) {
            throw new ProfileNotFoundException("Followers Profile with id: " + followerId + " doesn`t exist");
        }

        followerRepository.deleteById_AuthorIdAndId_FollowerId(authorId, followerId);
    }
}
