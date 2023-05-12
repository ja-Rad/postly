package com.jarad.postly.controller;

import com.jarad.postly.entity.Post;
import com.jarad.postly.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PostController {
    private final PostService postService;
    private final String PROFILE_SUBFOLDER_PREFIX = "post/";

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * READ Mappings
     */

    @GetMapping("/posts")
    public String getPaginatedPosts(@RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    Model model) {
        Page<Post> postPage = postService.returnPaginatedPostsByCreationDateDescending(page, size);
        int totalPages = postPage.getTotalPages();

        if (totalPages > 0) {
            List<Integer> pageNumbers = postService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("postPage", postPage);

        return PROFILE_SUBFOLDER_PREFIX + "posts";
    }

    /**
     * WRITE Mappings
     */
}
