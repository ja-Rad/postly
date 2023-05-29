package com.jarad.postly.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.User;
import com.jarad.postly.repository.CommentRepository;
import com.jarad.postly.repository.FollowerRepository;
import com.jarad.postly.repository.PostRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.repository.UserRepository;
import com.jarad.postly.util.dto.ProfileDto;
import com.jarad.postly.util.exception.ProfileForUserAlreadyExistException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import com.jarad.postly.util.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    public static final int PAGE_SIZE = 10;
    private final ProfileRepository profileRepository;
    private final FollowerRepository followerRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository, FollowerRepository followerRepository, PostRepository postRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.profileRepository = profileRepository;
        this.followerRepository = followerRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public Page<Profile> returnPaginatedProfilesByCreationDateDescending(int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        Page<Profile> pageProfile = profileRepository.findAll(pageable);
        return pageProfile;
    }

    @Override
    public Page<Post> returnProfilePaginatedPostsByCreationDateDescending(Long profileId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        Page<Post> pageProfilePosts = postRepository.findPostPageByProfile_Id(profileId, pageable);

        return pageProfilePosts;
    }

    @Override
    public Page<Follower> returnProfilePaginatedAuthorsByCreationDateDescending(Long profileId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        Page<Follower> pageProfileAuthors = followerRepository.findAuthorPageById_FollowerId(profileId, pageable);

        return pageProfileAuthors;
    }

    @Override
    public Page<Follower> returnProfilePaginatedFollowersByCreationDateDescending(Long profileId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        Page<Follower> pageProfileFollowers = followerRepository.findFollowerPageById_AuthorId(profileId, pageable);

        return pageProfileFollowers;
    }

    @Override
    public Page<Comment> returnProfilePaginatedCommentsByCreationDateDescending(Long profileId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        Page<Comment> pageProfileComments = commentRepository.findCommentPageByProfile_Id(profileId, pageable);

        return pageProfileComments;
    }

    @Override
    public List<Integer> returnListOfPageNumbers(int totalPages) {
        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(toList());
    }

    @Override
    public Profile returnProfileById(Long profileId) {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) {
            String message = "Profile with this id: " + profileId + " doesn`t exist";
            log.info(message);
            throw new ProfileNotFoundException(message);
        }

        return optionalProfile.get();
    }

    @Transactional
    @Override
    public Long createNewProfileAndReturnProfileId(Long userId, ProfileDto profileDto) {
        log.info("Creating a new profile for user with ID {}", userId);

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            String message = "User with id: " + userId + " doesn`t exist";
            log.info(message);
            throw new UserNotFoundException(message);
        }

        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(userId);
        if (optionalProfile.isPresent()) {
            String message = "Profile for user: " + userId + " already exist";
            log.info(message);
            throw new ProfileForUserAlreadyExistException(message);
        }

        User user = optionalUser.get();
        user.setActiveProfile(true);
        Profile profile = Profile.builder()
                .username(profileDto.getUsername())
                .creationDate(Instant.now())
                .user(user)
                .build();
        Profile savedProfile = profileRepository.save(profile);

        log.info("New profile created with ID {} for user with ID {}", savedProfile.getId(), userId);

        return savedProfile.getId();
    }

    @Transactional
    @Override
    public Long updateExistingProfile(Long profileId, ProfileDto profileDto) {
        log.info("Updating profile with ID {}", profileId);

        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) {
            String message = "Profile for user with id: " + profileId + " doesn`t exist";
            log.info(message);
            throw new ProfileNotFoundException(message);
        }

        Profile profile = optionalProfile.get();
        profile.setUsername(profileDto.getUsername());
        Profile savedProfile = profileRepository.save(profile);

        log.info("Profile with ID {} has been updated", profileId);

        return savedProfile.getId();
    }

    @Transactional
    @Override
    public void deleteExistingProfile(Long userId, Long profileId) {
        log.info("Deleting profile with ID {} for user with ID {}", profileId, userId);

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            String message = "User with id: " + userId + " doesn`t exist";
            log.info(message);
            throw new UserNotFoundException(message);
        }

        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(profileId);
        if (optionalProfile.isEmpty()) {
            String message = "Profile for user with id: " + profileId + " doesn`t exist";
            log.info(message);
            throw new ProfileNotFoundException(message);
        }

        User user = optionalUser.get();
        user.setActiveProfile(false);

        profileRepository.deleteByUserAndId(user, profileId);

        log.info("Profile with ID {} for user with ID {} has been deleted", profileId, userId);
    }

    @Override
    public boolean isProfileExistForUser(Long userId) {
        return profileRepository.existsByUser_Id(userId);
    }

    @Override
    public boolean isUserOwnsThisProfile(Long userId, Long profileId) {
        return profileRepository.existsByUser_IdAndId(userId, profileId);
    }

    @Override
    public Set<Long> returnAuthorsByUserId(Long userId) {
        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(userId);

        if (optionalProfile.isPresent()) {
            Profile profile = optionalProfile.get();

            return profile.getFollowers().stream()
                    .map(follower -> follower.getId().getAuthorId())
                    .collect(toSet());

        }

        return Collections.emptySet();
    }
}
