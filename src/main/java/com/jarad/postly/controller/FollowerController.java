package com.jarad.postly.controller;

import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.FollowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class FollowerController {

    private final FollowerService followerService;

    @Autowired
    public FollowerController(FollowerService followerService) {
        this.followerService = followerService;
    }
    
    /**
     * WRITE Mappings
     */
    @PostMapping("/followers/{id}")
    public String addFollower(@AuthenticationPrincipal UserDetailsImpl userDetails,
                              @PathVariable("id") Long authorId,
                              @RequestHeader(HttpHeaders.REFERER) String referer) {
        Long userId = userDetails.getUserId();
        followerService.addFollowerToAuthor(userId, authorId);
        String trimmedRefererPath = referer.replaceAll("(.*\\/{2})(.*?\\/)", "");

        return "redirect:/" + trimmedRefererPath;
    }

    @DeleteMapping("/followers/{id}")
    public String deleteFollower(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable("id") Long authorId,
                                 @RequestHeader(HttpHeaders.REFERER) String referer) {
        Long userId = userDetails.getUserId();
        followerService.deleteFollowerFromAuthor(userId, authorId);
        String trimmedRefererPath = referer.replaceAll("(.*\\/{2})(.*?\\/)", "");

        return "redirect:/" + trimmedRefererPath;
    }


}
