package com.jarad.postly.controller;

import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.FollowerService;
import com.jarad.postly.util.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
@Slf4j
public class FollowerController {

    private final FollowerService followerService;

    @Autowired
    public FollowerController(FollowerService followerService) {
        this.followerService = followerService;
    }

    /**
     * Helper method to trim URL, so that only Path/Page is left
     *
     * @param referer is a complete URL
     * @return partial URL with only a Path/Page part left
     */
    private static String getTrimmedRefererPath(String referer) {
        return referer.replaceAll("(.*\\/{2})(.*?\\/)", "");
    }

    /**
     * WRITE Mappings
     */
    @PostMapping("/followers/{id}")
    @LogExecutionTime
    public String addFollower(@AuthenticationPrincipal UserDetailsImpl userDetails,
                              @PathVariable("id") Long authorId,
                              @RequestHeader(HttpHeaders.REFERER) String referer) {
        log.info("Entering addFollower");

        Long userId = userDetails.getUserId();
        followerService.addFollowerToAuthor(userId, authorId);
        String trimmedRefererPath = getTrimmedRefererPath(referer);

        return "redirect:/" + trimmedRefererPath;
    }

    @DeleteMapping("/followers/{id}")
    @LogExecutionTime
    public String deleteFollower(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable("id") Long authorId,
                                 @RequestHeader(HttpHeaders.REFERER) String referer) {
        log.info("Entering deleteFollower");

        Long userId = userDetails.getUserId();
        followerService.deleteFollowerFromAuthor(userId, authorId);
        String trimmedRefererPath = getTrimmedRefererPath(referer);

        return "redirect:/" + trimmedRefererPath;
    }


}