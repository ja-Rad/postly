package com.jarad.postly.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.repository.CommentRepository;
import com.jarad.postly.repository.FollowerRepository;
import com.jarad.postly.repository.PostRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.repository.RoleRepository;
import com.jarad.postly.repository.UserRepository;
import com.jarad.postly.util.dto.ProfileDto;
import com.jarad.postly.util.enums.SecurityRole;
import com.jarad.postly.util.exception.ProfileForUserAlreadyExistException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import com.jarad.postly.util.exception.RoleNotFoundException;
import com.jarad.postly.util.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ProfileServiceImpl implements ProfileService {
    public static final int PAGE_SIZE = 10;
    public static final String CREATION_DATE = "creationDate";
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final FollowerRepository followerRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository, FollowerRepository followerRepository, PostRepository postRepository, UserRepository userRepository, CommentRepository commentRepository,
                              RoleRepository roleRepository) {
        this.profileRepository = profileRepository;
        this.followerRepository = followerRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public Page<Profile> returnPaginatedProfilesByCreationDateDescending(int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(CREATION_DATE).descending());
        return profileRepository.findAll(pageable);
    }

    @Override
    public Page<Post> returnProfilePaginatedPostsByCreationDateDescending(Long profileId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(CREATION_DATE).descending());
        return postRepository.findPostPageByProfileId(profileId, pageable);
    }

    @Override
    public Page<Follower> returnProfilePaginatedAuthorsByCreationDateDescending(Long profileId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(CREATION_DATE).descending());
        return followerRepository.findAuthorPageByIdFollowerId(profileId, pageable);
    }

    @Override
    public Page<Follower> returnProfilePaginatedFollowersByCreationDateDescending(Long profileId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(CREATION_DATE).descending());
        return followerRepository.findFollowerPageByIdAuthorId(profileId, pageable);
    }

    @Override
    public Page<Comment> returnProfilePaginatedCommentsByCreationDateDescending(Long profileId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(CREATION_DATE).descending());
        return commentRepository.findCommentPageByProfileId(profileId, pageable);
    }

    @Override
    public List<Integer> returnListOfPageNumbers(int totalPages) {
        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
    }

    @Override
    public Profile returnProfileById(Long profileId) {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) {
            String message = MessageFormat.format("Profile with ID {0} doesn''t exist", profileId);
            log.info(message);
            throw new ProfileNotFoundException(message);
        }

        return optionalProfile.get();
    }

    @Transactional
    @Override
    public void createNewProfileAndReturnProfileId(Long userId, ProfileDto profileDto) {
        log.info("Creating a new profile for user with ID {}", userId);

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            String message = MessageFormat.format("User with ID {0} doesn''t exist", userId);
            log.info(message);
            throw new UserNotFoundException(message);
        }

        Optional<Profile> optionalProfile = profileRepository.findByUserId(userId);
        if (optionalProfile.isPresent()) {
            String message = MessageFormat.format("Profile for user with ID {0} already exist", userId);
            log.info(message);
            throw new ProfileForUserAlreadyExistException(message);
        }

        User user = optionalUser.get();
        user.setActiveProfile(true);
        user.addRole(getOrCreateRole(SecurityRole.ROLE_PROFILE_ACTIVE));

        Profile profile = Profile.builder()
                .username(profileDto.getUsername())
                .creationDate(Instant.now())
                .user(user)
                .build();
        Profile savedProfile = profileRepository.save(profile);

        log.info("New profile created with ID {} for user with ID {}", savedProfile.getId(), userId);
    }

    @Transactional
    @Override
    public void updateExistingProfile(Long profileId, ProfileDto profileDto) {
        log.info("Updating profile with ID {}", profileId);

        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) {
            String message = MessageFormat.format("Profile for user with ID {0} doesn''t exist", profileId);
            log.info(message);
            throw new ProfileNotFoundException(message);
        }

        Profile profile = optionalProfile.get();
        profile.setUsername(profileDto.getUsername());
    }

    @Transactional
    @Override
    public void deleteExistingProfile(Long userId, Long profileId) {
        log.info("Deleting profile with ID {} for user with ID {}", profileId, userId);

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            String message = MessageFormat.format("User with ID {0} doesn''t exist", userId);
            log.info(message);
            throw new UserNotFoundException(message);
        }

        Optional<Profile> optionalProfile = profileRepository.findByUserId(profileId);
        if (optionalProfile.isEmpty()) {
            String message = MessageFormat.format("Profile for user with ID {0} doesn''t exist", profileId);
            log.info(message);
            throw new ProfileNotFoundException(message);
        }

        Optional<Role> optionalRole = roleRepository.findByName(SecurityRole.ROLE_PROFILE_ACTIVE.toString());
        if (optionalRole.isEmpty()) {
            String message = MessageFormat.format("Role for user with name {0} doesn''t exist", SecurityRole.ROLE_PROFILE_ACTIVE);
            log.warn(message);
            throw new RoleNotFoundException(message);
        }

        User user = optionalUser.get();
        Role role = optionalRole.get();
        
        profileRepository.deleteByUserAndId(user, profileId);

        user.setActiveProfile(false);
        user.removeRole(getOrCreateRole(SecurityRole.ROLE_PROFILE_ACTIVE));
        userRepository.save(user);
        log.info("Profile with ID {} for user with ID {} has been deleted", profileId, userId);
    }

    @Override
    public boolean isProfileExistForUser(Long userId) {
        return profileRepository.existsByUserId(userId);
    }

    @Override
    public boolean isUserOwnsThisProfile(Long userId, Long profileId) {
        return profileRepository.existsByUserIdAndId(userId, profileId);
    }

    @Override
    public Set<Long> returnAuthorsByUserId(Long userId) {
        Optional<Profile> optionalProfile = profileRepository.findByUserId(userId);

        if (optionalProfile.isPresent()) {
            Profile profile = optionalProfile.get();

            return profile.getFollowers().stream()
                    .map(follower -> follower.getId().getAuthorId())
                    .collect(toSet());

        }

        return Collections.emptySet();
    }

    /**
     * Helper method that checks if ROLE_USER already exists in Database. If not creates it
     *
     * @return ROLE_USER from table 'roles'
     */
    @Transactional
    public Role getOrCreateRole(SecurityRole securityRoleName) {
        log.info("Retrieving the {} role", securityRoleName);
        Optional<Role> optionalRole = roleRepository.findByName(securityRoleName.toString());

        if (optionalRole.isEmpty()) {
            log.info("Creating and saving the {} role", securityRoleName);
            Role roleUser = Role.builder()
                    .name(securityRoleName.toString())
                    .build();
            roleRepository.save(roleUser);
            return roleUser;
        }

        return optionalRole.get();
    }
}
