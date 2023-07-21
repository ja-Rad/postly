package com.jarad.postly.controller;

import com.jarad.postly.aspect.LogExecutionTime;
import com.jarad.postly.entity.Post;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.PostService;
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
    private static final String POST_SUBFOLDER_PREFIX = "post/";
    private final PostService postService;

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
        boolean activeProfile = userDetails.isActiveProfile();

        Set<Long> authorsByUserId = postService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Page<Post> postPage = postService.returnPaginatedPostsByCreationDateDescending(page - 1);

        int totalPages = postPage.getTotalPages();
        model.addAttribute("totalPages", totalPages);

        if (totalPages > 1) {
            List<Integer> pageNumbers = postService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("postPage", postPage);
        model.addAttribute("userId", userId);
        model.addAttribute("activeProfile", activeProfile);

        return POST_SUBFOLDER_PREFIX + "posts";
    }

    @GetMapping("/posts/{postId}")
    @LogExecutionTime
    public String getPostById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                              @PathVariable("postId") Long postId,
                              Model model) {
        log.info("Entering getPostById");

        Long userId = userDetails.getUserId();
        boolean activeProfile = userDetails.isActiveProfile();

        Set<Long> authorsByUserId = postService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Post post = postService.returnPostById(postId);

        if (postService.isPostOwnedByUser(userId, postId)) {
            model.addAttribute("personalPost", true);
        }
        model.addAttribute("post", post);
        model.addAttribute("userId", userId);
        model.addAttribute("activeProfile", activeProfile);

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

    @GetMapping("/posts/{postId}/update-form")
    @LogExecutionTime
    public String getPostUpdateForm(@PathVariable("postId") Long postId,
                                    Model model) {
        log.info("Entering getPostCreateForm");

        Post post = postService.returnPostById(postId);
        model.addAttribute("post", post);
        model.addAttribute("postId", postId);

        return POST_SUBFOLDER_PREFIX + "post-update-form";
    }

    /**
     * WRITE Mappings
     */
    @PostMapping("/posts")
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

    @PutMapping("/posts/{postId}")
    @LogExecutionTime
    public String updatePostById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable("postId") Long postId,
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

    @DeleteMapping("/posts/{postId}")
    @LogExecutionTime
    public String deletePostById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable("postId") Long postId) {
        log.info("Entering deletePostById");

        Long userId = userDetails.getUserId();
        postService.deleteExistingPost(userId, postId);
        return "redirect:/profiles/" + postId + "/posts";
    }
}
