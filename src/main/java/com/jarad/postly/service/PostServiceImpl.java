package com.jarad.postly.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.repository.CommentRepository;
import com.jarad.postly.repository.PostRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.util.dto.CommentDto;
import com.jarad.postly.util.dto.PostDto;
import com.jarad.postly.util.exception.PostNotFoundException;
import com.jarad.postly.util.exception.ProfileNotAllowedToUpdateThisPost;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.notEqual;

@Service
public class PostServiceImpl implements PostService {
    public static final int PAGE_SIZE = 10;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, ProfileRepository profileRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public List<Integer> returnListOfPageNumbers(int totalPages) {
        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(toList());
    }

    @Override
    public Page<Post> returnPaginatedPostsByCreationDateDescending(int page, int size) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return postRepository.findAll(pageable);
    }

    @Override
    public Page<Comment> returnPaginatedCommentsByCreationDateDescending(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return commentRepository.findByPost_Id(postId, pageable);
    }

    @Override
    public Post returnPostById(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            throw new PostNotFoundException("Post with this id: " + postId + " doesn`t exist");
        }

        return optionalPost.get();
    }

    @Override
    public Long createNewPostAndReturnPostId(Long userId, PostDto postDto) {
        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(userId);
        if (optionalProfile.isEmpty()) {
            throw new ProfileNotFoundException("Profile with id: " + userId + " doesn`t exist");
        }

        Post post = Post.builder()
                .title(postDto.getTitle())
                .description(postDto.getDescription())
                .creationDate(Instant.now())
                .profile(optionalProfile.get())
                .build();
        Post savedPost = postRepository.save(post);

        return savedPost.getId();
    }

    @Override
    public void updateExistingPost(Long profileId, Long postId, PostDto postDto) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            throw new PostNotFoundException("Post for user with id: " + postId + " doesn`t exist");
        }

        Post post = optionalPost.get();
        if (notEqual(profileId, post.getProfile().getId())) {
            throw new ProfileNotAllowedToUpdateThisPost("Profile with id: " + profileId + " not allowed to update post with id + " + postId);
        }

        post.setTitle(postDto.getTitle());
        post.setDescription(postDto.getDescription());

        postRepository.save(post);
    }

    @Override
    public void deleteExistingPost(Long profileId, Long postId) {
        Optional<Post> optionalPost = postRepository.findByProfile_IdAndId(profileId, postId);
        if (optionalPost.isEmpty()) {
            throw new PostNotFoundException("Post with id: " + postId + " doesn`t exist for profile with id: " + profileId);
        }

        Post post = optionalPost.get();
        postRepository.delete(post);
    }

    @Override
    public boolean isPostOwnedByUser(Long userId, Long postId) {
        return postRepository.existsByProfile_User_IdAndId(userId, postId);
    }

    @Override
    public Long createNewCommentAndReturnCommentId(Long postId, Long userId, CommentDto commentDto) {
        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(userId);
        if (optionalProfile.isEmpty()) {
            throw new ProfileNotFoundException("Profile with id: " + userId + " doesn`t exist");
        }

        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            throw new PostNotFoundException("Post with id: " + userId + " doesn`t exist");
        }

        Comment comment = Comment.builder()
                .description(commentDto.getDescription())
                .creationDate(Instant.now())
                .profile(optionalProfile.get())
                .post(optionalPost.get())
                .build();
        Comment savedComment = commentRepository.save(comment);

        return savedComment.getId();
    }

    @Override
    public Set<Long> returnAuthorsByUserId(Long userId) {
        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(userId);
        if (optionalProfile.isEmpty()) {
            throw new ProfileNotFoundException("Profile with id: " + userId + " doesn`t exist");
        }

        Profile profile = optionalProfile.get();
        return profile.getFollowers().stream()
                .map(follower -> follower.getId().getAuthorId())
                .collect(toSet());
    }
}
