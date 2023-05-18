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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Service
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
    public Page<Profile> returnPaginatedProfilesByCreationDateDescending(int page, int size) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return profileRepository.findAll(pageable);
    }

    @Override
    public Page<Post> returnProfilePaginatedPostsByCreationDateDescending(Long id, int page, int size) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return postRepository.findPostPageByProfile_Id(id, pageable);
    }

    @Override
    public Page<Follower> returnProfilePaginatedAuthorsByCreationDateDescending(Long id, int page, int size) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return followerRepository.findAuthorPageById_FollowerId(id, pageable);
    }

    @Override
    public Page<Follower> returnProfilePaginatedFollowersByCreationDateDescending(Long id, int page, int size) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return followerRepository.findFollowerPageById_AuthorId(id, pageable);
    }

    @Override
    public Page<Comment> returnProfilePaginatedCommentsByCreationDateDescending(Long id, int page, int size) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return commentRepository.findCommentPageByProfile_Id(id, pageable);
    }

    @Override
    public List<Integer> returnListOfPageNumbers(int totalPages) {
        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(toList());
    }

    @Override
    public Profile returnProfileById(Long id) {
        Optional<Profile> optionalProfile = profileRepository.findById(id);
        if (optionalProfile.isEmpty()) {
            throw new ProfileNotFoundException("Profile with this id: " + id + " doesn`t exist");
        }

        return optionalProfile.get();
    }

    @Override
    public Long createNewProfileAndReturnProfileId(Long id, ProfileDto profileDto) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User with id: " + id + " doesn`t exist");
        }
        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(id);
        if (optionalProfile.isPresent()) {
            throw new ProfileForUserAlreadyExistException("Profile for user: " + id + " already exist");
        }

        User user = optionalUser.get();
        user.setActiveProfile(true);
        Profile profile = Profile.builder()
                .username(profileDto.getUsername())
                .creationDate(Instant.now())
                .user(user)
                .build();
        Profile savedProfile = profileRepository.save(profile);

        return savedProfile.getId();
    }

    @Override
    public Long updateExistingProfile(Long id, ProfileDto profileDto) {
        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(id);
        if (optionalProfile.isEmpty()) {
            throw new ProfileNotFoundException("Profile for user with id: " + id + " doesn`t exist");
        }

        Profile profile = optionalProfile.get();
        profile.setUsername(profileDto.getUsername());
        Profile savedProfile = profileRepository.save(profile);

        return savedProfile.getId();
    }

    @Override
    public void deleteExistingProfile(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User with id: " + id + " doesn`t exist");
        }
        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(id);
        if (optionalProfile.isEmpty()) {
            throw new ProfileNotFoundException("Profile for user with id: " + id + " doesn`t exist");
        }

        User user = optionalUser.get();
        user.setActiveProfile(false);
        Profile profile = optionalProfile.get();
        profile.setUser(user);

        profileRepository.delete(profile);
    }

    @Override
    public boolean isProfileExistForUser(Long id) {
        return profileRepository.existsByUser_Id(id);
    }
}
