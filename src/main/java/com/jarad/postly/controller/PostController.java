package com.jarad.postly.controller;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Post;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.PostService;
import com.jarad.postly.util.dto.CommentDto;
import com.jarad.postly.util.dto.PostDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Controller
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
    public String getPaginatedPosts(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    Model model) {
        Long userId = userDetails.getUserId();
        model.addAttribute("userId", userId);

        Set<Long> authorsByUserId = postService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Page<Post> postPage = postService.returnPaginatedPostsByCreationDateDescending(page - 1, size);
        int totalPages = postPage.getTotalPages();

        if (totalPages > 0) {
            List<Integer> pageNumbers = postService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("postPage", postPage);

        return POST_SUBFOLDER_PREFIX + "posts";
    }

    @GetMapping("/posts/{id}")
    public String getPostById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                              @PathVariable Long id,
                              Model model) {

        Long userId = userDetails.getUserId();
        Post post = postService.returnPostById(id);
        Long postProfileId = post.getProfile().getId();
        model.addAttribute("personalPost", false);

        if (Objects.equals(userId, postProfileId)) {
            model.addAttribute("personalPost", true);
            model.addAttribute("postId", id);
        }

        model.addAttribute("post", post);

        return POST_SUBFOLDER_PREFIX + "post";
    }

    @GetMapping("/posts/create-form")
    public String getPostCreateForm(Model model) {
        PostDto postDto = new PostDto();
        model.addAttribute("post", postDto);

        return POST_SUBFOLDER_PREFIX + "post-create-form";
    }

    @GetMapping("/posts/{id}/update-form")
    public String getPostUpdateForm(@PathVariable Long id,
                                    Model model) {
        Post post = postService.returnPostById(id);
        model.addAttribute("post", post);
        model.addAttribute("postId", id);

        return POST_SUBFOLDER_PREFIX + "post-update-form";
    }

    @GetMapping("/posts/{id}/comments")
    public String getPostCommentsById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @PathVariable Long id,
                                      Model model,
                                      @RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "size", defaultValue = "10") int size) {

        Long userId = userDetails.getUserId();
        model.addAttribute("userId", userId);
        
        Set<Long> authorsByUserId = postService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Page<Comment> commentPage = postService.returnPaginatedCommentsByCreationDateDescending(id, page - 1, size);
        int totalPages = commentPage.getTotalPages();

        if (totalPages > 0) {
            List<Integer> pageNumbers = postService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("postId", id);
        // attribute comment.profile.username.id == profileId then edit/delete

        return POST_SUBFOLDER_PREFIX + "post-comments";
    }

    @GetMapping("/posts/{id}/comments/create-form")
    public String getPostCommentsById(@PathVariable Long id,
                                      Model model) {
        CommentDto commentDto = new CommentDto();
        model.addAttribute("comment", commentDto);
        model.addAttribute("postId", id);

        return POST_SUBFOLDER_PREFIX + "post-comment-create-form";
    }

    /**
     * WRITE Mappings
     */
    @PostMapping("posts")
    public String addPost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                          @ModelAttribute("post") @Valid PostDto postDto) {
        Long userId = userDetails.getUserId();
        Long postId = postService.createNewPostAndReturnPostId(userId, postDto);

        return "redirect:/posts/" + postId;
    }

    @PostMapping("/posts/{id}/comments/create-form")
    public String addPostCommentById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @ModelAttribute("comment") @Valid CommentDto commentDto,
                                     @PathVariable("id") Long postId) {
        Long userId = userDetails.getUserId();
        Long commentId = postService.createNewCommentAndReturnCommentId(postId, userId, commentDto);

        return "redirect:/comments/" + commentId;
    }

    @PutMapping("posts/{id}")
    public String updatePostById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable("id") Long id,
                                 @ModelAttribute("post") @Valid PostDto postDto) {
        Long userId = userDetails.getUserId();
        Long postId = postService.updateExistingPost(userId, id, postDto);

        return "redirect:/posts/" + postId;
    }

    @DeleteMapping("posts/{id}")
    public String deletePostById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable("id") Long id) {

        Long userId = userDetails.getUserId();
        postService.deleteExistingPost(userId, id);
        return "redirect:/profiles/%s/posts".formatted(userId);
    }
}
