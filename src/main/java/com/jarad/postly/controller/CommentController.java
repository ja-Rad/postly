package com.jarad.postly.controller;


import com.jarad.postly.aspect.LogExecutionTime;
import com.jarad.postly.entity.Comment;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.CommentService;
import com.jarad.postly.util.dto.CommentDto;
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
public class CommentController {

    public static final String POST_ID = "postId";
    public static final String COMMENT = "comment";
    public static final String COMMENT_ID = "commentId";
    private static final String COMMENT_SUBFOLDER_PREFIX = "comment/";
    private CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * READ Mappings
     */
    @GetMapping("/posts/{postId}/comments")
    @LogExecutionTime
    public String getPostCommentsById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @PathVariable(POST_ID) Long postId,
                                      Model model,
                                      @RequestParam(value = "page", defaultValue = "1") int page) {
        log.info("Entering getPostCommentsById");

        Long userId = userDetails.getUserId();
        boolean activeProfile = userDetails.isActiveProfile();

        Set<Long> authorsByUserId = commentService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Page<Comment> commentPage = commentService.returnPaginatedCommentsByCreationDateDescending(postId, page - 1);
        int totalPages = commentPage.getTotalPages();

        if (totalPages > 1) {
            List<Integer> pageNumbers = commentService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("userId", userId);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute(POST_ID, postId);

        return COMMENT_SUBFOLDER_PREFIX + "comments";
    }

    @GetMapping("/posts/{postId}/comments/{commentId}")
    @LogExecutionTime
    public String getCommentById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable(POST_ID) Long postId,
                                 @PathVariable(COMMENT_ID) Long commentId,
                                 Model model) {
        log.info("Entering getCommentById");

        Long userId = userDetails.getUserId();
        boolean activeProfile = userDetails.isActiveProfile();

        Set<Long> authorsByUserId = commentService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Comment comment = commentService.returnCommentById(postId, commentId);

        if (commentService.isCommentOwnedByUser(userId, commentId)) {
            model.addAttribute("personalComment", true);
        }
        model.addAttribute(COMMENT, comment);
        model.addAttribute("userId", userId);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute(POST_ID, postId);
        model.addAttribute(COMMENT_ID, commentId);

        return COMMENT_SUBFOLDER_PREFIX + COMMENT;
    }

    @GetMapping("/posts/{postId}/comments/create-form")
    @LogExecutionTime
    public String getPostCommentsById(@PathVariable(POST_ID) Long postId,
                                      Model model) {
        log.info("Entering getPostCommentsById");

        CommentDto commentDto = new CommentDto();
        model.addAttribute(POST_ID, postId);
        model.addAttribute(COMMENT, commentDto);

        return COMMENT_SUBFOLDER_PREFIX + "comment-create-form";
    }

    @GetMapping("/posts/{postId}/comments/{commentId}/update-form")
    @LogExecutionTime
    public String getPostUpdateForm(@PathVariable(POST_ID) Long postId,
                                    @PathVariable(COMMENT_ID) Long commentId,
                                    Model model) {
        log.info("Entering getPostUpdateForm");

        Comment comment = commentService.returnCommentById(postId, commentId);
        model.addAttribute(COMMENT, comment);
        model.addAttribute(POST_ID, postId);
        model.addAttribute(COMMENT_ID, commentId);

        return COMMENT_SUBFOLDER_PREFIX + "comment-update-form";
    }

    /**
     * WRITE Mappings
     */
    @PostMapping("/posts/{postId}/comments/create-form")
    @LogExecutionTime
    public String addPostCommentById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @PathVariable(POST_ID) Long postId,
                                     @ModelAttribute(COMMENT) @Valid CommentDto commentDto,
                                     BindingResult bindingResult,
                                     Model model) {
        log.info("Entering addPostCommentById");

        if (bindingResult.hasErrors()) {
            log.info("Validation errors occurred");

            model.addAttribute(POST_ID, postId);
            return COMMENT_SUBFOLDER_PREFIX + "comment-create-form";
        }

        Long userId = userDetails.getUserId();
        Long commentId = commentService.createNewCommentAndReturnCommentId(userId, postId, commentDto);

        return "redirect:/posts/" + postId + "/comments/" + commentId;
    }

    @PutMapping("/posts/{postId}/comments/{commentId}")
    @LogExecutionTime
    public String updateCommentById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @PathVariable(POST_ID) Long postId,
                                    @PathVariable(COMMENT_ID) Long commentId,
                                    @ModelAttribute(COMMENT) @Valid CommentDto commentDto,
                                    BindingResult bindingResult,
                                    Model model) {
        log.info("Entering updateCommentById");

        if (bindingResult.hasErrors()) {
            log.info("Validation errors occurred");

            model.addAttribute(POST_ID, postId);
            model.addAttribute(COMMENT_ID, commentId);
            return COMMENT_SUBFOLDER_PREFIX + "comment-update-form";
        }

        Long userId = userDetails.getUserId();
        commentService.updateExistingComment(userId, postId, commentId, commentDto);

        return "redirect:/posts/" + postId + "/comments/" + commentId;
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    @LogExecutionTime
    public String deleteCommentById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @PathVariable(POST_ID) Long postId,
                                    @PathVariable(COMMENT_ID) Long commentId) {
        log.info("Entering deleteCommentById");

        Long userId = userDetails.getUserId();
        commentService.deleteExistingComment(userId, postId, commentId);

        return "redirect:/profiles/" + userId + "/comments";
    }
}
