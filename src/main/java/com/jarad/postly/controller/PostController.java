package com.jarad.postly.controller;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Post;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.PostService;
import com.jarad.postly.util.annotation.LogExecutionTime;
import com.jarad.postly.util.dto.CommentDto;
import com.jarad.postly.util.dto.PostDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Controller
@Slf4j
public class PostController {
    private final PostService postService;
    private final String POST_SUBFOLDER_PREFIX = "post/";

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * READ Mappings
     */
    @GetMapping("/posts")
    @LogExecutionTime
    public String getPaginatedPosts(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                    Model model) {
        log.info("Entering getPaginatedPosts");

        Long userId = userDetails.getUserId();
        Set<Long> authorsByUserId = postService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Page<Post> postPage = postService.returnPaginatedPostsByCreationDateDescending(page - 1);
        int totalPages = postPage.getTotalPages();

        if (totalPages > 1) {
            List<Integer> pageNumbers = postService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("postPage", postPage);

        return POST_SUBFOLDER_PREFIX + "posts";
    }

    @GetMapping("/posts/{id}")
    @LogExecutionTime
    public String getPostById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                              @PathVariable("id") Long postId,
                              Model model) {
        log.info("Entering getPostById");

        Long userId = userDetails.getUserId();
        Post post = postService.returnPostById(postId);

        if (postService.isPostOwnedByUser(userId, postId)) {
            model.addAttribute("personalPost", true);
        }
        model.addAttribute("post", post);

        return POST_SUBFOLDER_PREFIX + "post";
    }

    @GetMapping("/posts/create-form")
    @LogExecutionTime
    public String getPostCreateForm(Model model) {
        log.info("Entering getPostCreateForm");

        PostDto postDto = new PostDto();
        model.addAttribute("post", postDto);

        return POST_SUBFOLDER_PREFIX + "post-create-form";
    }

    @GetMapping("/posts/{id}/update-form")
    @LogExecutionTime
    public String getPostUpdateForm(@PathVariable("id") Long postId,
                                    Model model) {
        log.info("Entering getPostCreateForm");

        Post post = postService.returnPostById(postId);
        model.addAttribute("post", post);
        model.addAttribute("postId", postId);

        return POST_SUBFOLDER_PREFIX + "post-update-form";
    }

    @GetMapping("/posts/{id}/comments")
    @LogExecutionTime
    public String getPostCommentsById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @PathVariable("id") Long postId,
                                      Model model,
                                      @RequestParam(value = "page", defaultValue = "1") int page) {
        log.info("Entering getPostCommentsById");

        Long userId = userDetails.getUserId();
        Set<Long> authorsByUserId = postService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Page<Comment> commentPage = postService.returnPaginatedCommentsByCreationDateDescending(postId, page - 1);
        int totalPages = commentPage.getTotalPages();

        if (totalPages > 1) {
            List<Integer> pageNumbers = postService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("postId", postId);

        return POST_SUBFOLDER_PREFIX + "post-comments";
    }

    @GetMapping("/posts/{id}/comments/create-form")
    @LogExecutionTime
    public String getPostCommentsById(@PathVariable("id") Long postId,
                                      Model model) {
        log.info("Entering getPostCommentsById");

        CommentDto commentDto = new CommentDto();
        model.addAttribute("comment", commentDto);
        model.addAttribute("postId", postId);

        return POST_SUBFOLDER_PREFIX + "post-comment-create-form";
    }

    /**
     * WRITE Mappings
     */
    @PostMapping("posts")
    @LogExecutionTime
    public String addPost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                          @ModelAttribute("post") @Valid PostDto postDto,
                          BindingResult bindingResult) {
        log.info("Entering addPost");

        if (bindingResult.hasErrors()) {
            log.info("Validation errors occurred");

            return POST_SUBFOLDER_PREFIX + "post-create-form";
        }

        Long userId = userDetails.getUserId();
        Long postId = postService.createNewPostAndReturnPostId(userId, postDto);

        return "redirect:/posts/" + postId;
    }

    @PutMapping("posts/{id}")
    @LogExecutionTime
    public String updatePostById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable("id") Long postId,
                                 @ModelAttribute("post") @Valid PostDto postDto,
                                 BindingResult bindingResult,
                                 Model model) {
        log.info("Entering updatePostById");

        if (bindingResult.hasErrors()) {
            log.info("Validation errors occurred");

            model.addAttribute("postId", postId);
            return POST_SUBFOLDER_PREFIX + "post-update-form";
        }

        Long userId = userDetails.getUserId();
        postService.updateExistingPost(userId, postId, postDto);

        return "redirect:/posts/" + postId;
    }

    @DeleteMapping("posts/{id}")
    @LogExecutionTime
    public String deletePostById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable("id") Long postId) {
        log.info("Entering deletePostById");

        Long userId = userDetails.getUserId();
        postService.deleteExistingPost(userId, postId);
        return "redirect:/profiles/" + postId + "/posts";
    }

    @PostMapping("/posts/{id}/comments/create-form")
    @LogExecutionTime
    public String addPostCommentById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @PathVariable("id") Long postId,
                                     @ModelAttribute("comment") @Valid CommentDto commentDto,
                                     BindingResult bindingResult,
                                     Model model) {
        log.info("Entering addPostCommentById");

        if (bindingResult.hasErrors()) {
            log.info("Validation errors occurred");

            model.addAttribute("postId", postId);
            return POST_SUBFOLDER_PREFIX + "post-comment-create-form";
        }

        Long userId = userDetails.getUserId();
        Long commentId = postService.createNewCommentAndReturnCommentId(userId, postId, commentDto);

        return "redirect:/comments/" + commentId;
    }
}
