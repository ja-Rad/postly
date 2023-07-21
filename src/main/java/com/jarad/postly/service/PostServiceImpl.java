package com.jarad.postly.service;

import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.repository.PostRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.util.dto.PostDto;
import com.jarad.postly.util.exception.PostNotFoundException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
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
public class PostServiceImpl implements PostService {
    public static final int PAGE_SIZE = 10;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, ProfileRepository profileRepository) {
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public List<Integer> returnListOfPageNumbers(int totalPages) {
        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
    }

    @Override
    public Page<Post> returnPaginatedPostsByCreationDateDescending(int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return postRepository.findAll(pageable);
    }

    @Override
    public Post returnPostById(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            String message = MessageFormat.format("Post with ID {0} doesn''t exist", postId);
            log.info(message);
            throw new PostNotFoundException(message);
        }

        return optionalPost.get();
    }

    @Transactional
    @Override
    public Long createNewPostAndReturnPostId(Long userId, PostDto postDto) {
        log.info("Creating a new post for user with ID {}", userId);

        Optional<Profile> optionalProfile = profileRepository.findByUserId(userId);
        if (optionalProfile.isEmpty()) {
            String message = MessageFormat.format("Profile with ID {0} doesn''t exist", userId);
            log.info(message);
            throw new ProfileNotFoundException(message);
        }

        Post post = Post.builder()
                .title(postDto.getTitle())
                .description(postDto.getDescription())
                .creationDate(Instant.now())
                .profile(optionalProfile.get())
                .build();
        Post savedPost = postRepository.save(post);

        log.info("New post created with ID {} for user with ID {}", savedPost.getId(), userId);

        return savedPost.getId();
    }

    @Transactional
    @Override
    public void updateExistingPost(Long userId, Long postId, PostDto postDto) {
        log.info("Updating post with ID {} owned by user with ID {}", postId, userId);

        Optional<Post> optionalPost = postRepository.findByProfileUserIdAndId(userId, postId);
        if (optionalPost.isEmpty()) {
            String message = MessageFormat.format("Post with ID {0} that is owned by user with ID {1} doesn''t exist", postId, userId);
            log.info(message);
            throw new PostNotFoundException(message);
        }

        Post post = optionalPost.get();
        post.setTitle(postDto.getTitle());
        post.setDescription(postDto.getDescription());

        postRepository.save(post);

        log.info("Post with ID {} owned by user with ID {} has been updated", postId, userId);

    }

    @Transactional
    @Override
    public void deleteExistingPost(Long profileId, Long postId) {
        Optional<Post> optionalPost = postRepository.findByProfileIdAndId(profileId, postId);
        if (optionalPost.isEmpty()) {
            String message = MessageFormat.format("Post with ID {0} doesn''t exist for profile with ID {1}", postId, profileId);
            log.info(message);
            throw new PostNotFoundException(message);
        }

        Post post = optionalPost.get();
        postRepository.delete(post);
    }

    @Override
    public boolean isPostOwnedByUser(Long userId, Long postId) {
        return postRepository.existsByProfileUserIdAndId(userId, postId);
    }

    @Override
    public String returnTitleByPostId(Long postId) {
        log.info("Returning post's title with ID {}", postId);

        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            String message = MessageFormat.format("Post with ID {0} doesn''t exist", postId);
            log.info(message);
            throw new PostNotFoundException(message);
        }

        Post post = optionalPost.get();

        return post.getTitle();
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
}
