package com.jarad.postly.controller;


import com.jarad.postly.entity.Comment;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.CommentService;
import com.jarad.postly.util.annotation.LogExecutionTime;
import com.jarad.postly.util.dto.CommentDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
public class CommentController {

    private final String COMMENT_SUBFOLDER_PREFIX = "comment/";
    private CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * READ Mappings
     */
    @GetMapping("/comments/{id}")
    @LogExecutionTime
    public String getCommentById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable("id") Long commentId,
                                 Model model) {
        Long userId = userDetails.getUserId();
        Comment comment = commentService.returnCommentById(commentId);

        if (commentService.isCommentOwnedByUser(userId, commentId)) {
            model.addAttribute("personalComment", true);
        }
        model.addAttribute("comment", comment);

        return COMMENT_SUBFOLDER_PREFIX + "comment";
    }

    @GetMapping("/comments/{id}/update-form")
    @LogExecutionTime
    public String getPostUpdateForm(@PathVariable("id") Long commentId,
                                    Model model) {
        Comment comment = commentService.returnCommentById(commentId);
        model.addAttribute("comment", comment);
        model.addAttribute("commentId", commentId);

        return COMMENT_SUBFOLDER_PREFIX + "comment-update-form";
    }

    /**
     * WRITE Mappings
     */
    @PutMapping("comments/{id}")
    @LogExecutionTime
    public String updateCommentById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @PathVariable("id") Long commentId,
                                    @ModelAttribute("comment") @Valid CommentDto commentDto,
                                    BindingResult bindingResult,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("commentId", commentId);
            return COMMENT_SUBFOLDER_PREFIX + "comment-update-form";
        }

        Long userId = userDetails.getUserId();
        commentService.updateExistingComment(userId, commentId, commentDto);

        return "redirect:/comments/" + commentId;
    }

    @DeleteMapping("comments/{id}")
    @LogExecutionTime
    public String deleteCommentById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @PathVariable("id") Long commentId) {
        Long userId = userDetails.getUserId();
        commentService.deleteExistingComment(userId, commentId);

        return "redirect:/profiles/" + userId + "/comments";
    }
}
