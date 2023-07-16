package com.jarad.postly.controller;

import com.jarad.postly.aspect.LogExecutionTime;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
public class SearchController {
    private static final String SEARCH_SUBFOLDER_PREFIX = "search/";
    private SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    @LogExecutionTime
    public String returnSearchResultPage(@RequestParam(value = "q") String query,
                                         @RequestParam(value = "type", defaultValue = "posts") String type,
                                         @RequestParam(value = "page", defaultValue = "1") int page,
                                         Model model) {
        log.info("Entering returnSearchResultPage");

        model.addAttribute("query", query);

        if (type.equals("posts")) {
            Page<Post> postPage = searchService.findPaginatedPostsContainingTitleByCreationDateDescending(query, page - 1);
            int totalPostPages = postPage.getTotalPages();
            model.addAttribute("totalPostPages", totalPostPages);

            if (totalPostPages > 1) {
                List<Integer> postPageNumbers = searchService.returnListOfPageNumbers(totalPostPages);
                model.addAttribute("postPageNumbers", postPageNumbers);
            }
            model.addAttribute("postPage", postPage);

        } else if (type.equals("profiles")) {
            Page<Profile> profilePage = searchService.findPaginatedProfilesContainingUsernameByCreationDateDescending(query, page - 1);
            int totalProfilePages = profilePage.getTotalPages();
            model.addAttribute("totalProfilePages", totalProfilePages);

            if (totalProfilePages > 1) {
                List<Integer> profilePageNumbers = searchService.returnListOfPageNumbers(totalProfilePages);
                model.addAttribute("profilePageNumbers", profilePageNumbers);
            }
            model.addAttribute("profilePage", profilePage);
        }

        return SEARCH_SUBFOLDER_PREFIX + "index";
    }
}
