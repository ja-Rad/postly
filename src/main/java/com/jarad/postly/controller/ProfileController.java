package com.jarad.postly.controller;

import com.jarad.postly.aspect.LogExecutionTime;
import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.ProfileService;
import com.jarad.postly.util.dto.ProfileDto;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class ProfileController {

    public static final String USER_ID = "userId";
    public static final String PROFILE_ID = "profileId";
    public static final String PAGE_NUMBERS = "pageNumbers";
    public static final String PROFILE = "profile";
    public static final String PERSONAL_PROFILE = "personalProfile";
    private static final String PROFILE_SUBFOLDER_PREFIX = "profile/";
    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * READ Mappings
     */
    @GetMapping("/profiles")
    @LogExecutionTime
    public String getPaginatedProfiles(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                       Model model) {
        log.info("Entering getPaginatedProfiles");

        Long userId = userDetails.getUserId();
        boolean activeProfile = userDetails.isActiveProfile();
        model.addAttribute(USER_ID, userId);

        Set<Long> authorsByUserId = profileService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Page<Profile> profilePage = profileService.returnPaginatedProfilesByCreationDateDescending(page - 1);
        model.addAttribute("profilePage", profilePage);

        int totalPages = profilePage.getTotalPages();
        model.addAttribute("totalPages", totalPages);

        if (totalPages > 1) {
            List<Integer> pageNumbers = profileService.returnListOfPageNumbers(totalPages);
            model.addAttribute(PAGE_NUMBERS, pageNumbers);
        }
        model.addAttribute(USER_ID, userId);
        model.addAttribute("activeProfile", activeProfile);

        return PROFILE_SUBFOLDER_PREFIX + "profiles";
    }

    @GetMapping("/profiles/{profileId}")
    @LogExecutionTime
    public String getProfileById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable("profileId") Long profileId,
                                 Model model) {
        log.info("Entering getProfileById");

        Long userId = userDetails.getUserId();
        boolean activeProfile = userDetails.isActiveProfile();

        Set<Long> authorsByUserId = profileService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Profile profile = profileService.returnProfileById(profileId);
        model.addAttribute(PROFILE, profile);

        Post latestPost = profileService.returnLatestPostById(profileId);
        model.addAttribute("latestPost", latestPost);

        Comment latestComment = profileService.returnLatestCommentById(profileId);
        model.addAttribute("latestComment", latestComment);

        if (profileService.isUserOwnsThisProfile(userId, profileId)) {
            model.addAttribute(PERSONAL_PROFILE, true);
        }
        model.addAttribute(USER_ID, userId);
        model.addAttribute("activeProfile", activeProfile);

        return PROFILE_SUBFOLDER_PREFIX + PROFILE;
    }

    @GetMapping("/profiles/{profileId}/posts")
    @LogExecutionTime
    public String getProfilePaginatedPosts(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @PathVariable("profileId") Long profileId,
                                           @RequestParam(value = "page", defaultValue = "1") int page,
                                           Model model) {
        log.info("Entering getProfilePosts");

        model.addAttribute(PROFILE_ID, profileId);

        String profileUsername = profileService.returnProfileUsername(profileId);
        model.addAttribute("profileUsername", profileUsername);

        Page<Post> postPage = profileService.returnProfilePaginatedPostsByCreationDateDescending(profileId, page - 1);
        model.addAttribute("postPage", postPage);

        int totalPages = postPage.getTotalPages();
        model.addAttribute("totalPages", totalPages);

        if (totalPages > 1) {
            List<Integer> pageNumbers = profileService.returnListOfPageNumbers(totalPages);
            model.addAttribute(PAGE_NUMBERS, pageNumbers);
        }

        return PROFILE_SUBFOLDER_PREFIX + "profile-posts";
    }

    @GetMapping("/profiles/{profileId}/comments")
    @LogExecutionTime
    public String getProfilePaginatedComments(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @PathVariable("profileId") Long profileId,
                                              @RequestParam(value = "page", defaultValue = "1") int page,
                                              Model model) {
        log.info("Entering getProfileComments");

        model.addAttribute(PROFILE_ID, profileId);

        String profileUsername = profileService.returnProfileUsername(profileId);
        model.addAttribute("profileUsername", profileUsername);

        Page<Comment> commentPage = profileService.returnProfilePaginatedCommentsByCreationDateDescending(profileId, page - 1);
        model.addAttribute("commentPage", commentPage);

        int totalPages = commentPage.getTotalPages();
        model.addAttribute("totalPages", totalPages);

        if (totalPages > 1) {
            List<Integer> pageNumbers = profileService.returnListOfPageNumbers(totalPages);
            model.addAttribute(PAGE_NUMBERS, pageNumbers);
        }

        return PROFILE_SUBFOLDER_PREFIX + "profile-comments";
    }

    @GetMapping("/profiles/{profileId}/authors")
    @LogExecutionTime
    public String getProfilePaginatedAuthors(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @PathVariable("profileId") Long profileId,
                                             @RequestParam(value = "page", defaultValue = "1") int page,
                                             Model model) {
        log.info("Entering getProfileAuthors");

        model.addAttribute(PROFILE_ID, profileId);

        String profileUsername = profileService.returnProfileUsername(profileId);
        model.addAttribute("profileUsername", profileUsername);

        Page<Follower> authorPage = profileService.returnProfilePaginatedAuthorsByCreationDateDescending(profileId, page - 1);
        model.addAttribute("authorPage", authorPage);

        int totalPages = authorPage.getTotalPages();
        model.addAttribute("totalPages", totalPages);

        if (totalPages > 1) {
            List<Integer> pageNumbers = profileService.returnListOfPageNumbers(totalPages);
            model.addAttribute(PAGE_NUMBERS, pageNumbers);
        }

        return PROFILE_SUBFOLDER_PREFIX + "profile-authors";
    }

    @GetMapping("/profiles/{profileId}/followers")
    @LogExecutionTime
    public String getProfilePaginatedFollowers(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @PathVariable("profileId") Long profileId,
                                               @RequestParam(value = "page", defaultValue = "1") int page,
                                               Model model) {
        log.info("Entering getProfileFollowers");

        model.addAttribute(PROFILE_ID, profileId);

        String profileUsername = profileService.returnProfileUsername(profileId);
        model.addAttribute("profileUsername", profileUsername);

        Page<Follower> followerPage = profileService.returnProfilePaginatedFollowersByCreationDateDescending(profileId, page - 1);
        model.addAttribute("followerPage", followerPage);

        int totalPages = followerPage.getTotalPages();
        model.addAttribute("totalPages", totalPages);

        if (totalPages > 1) {
            List<Integer> pageNumbers = profileService.returnListOfPageNumbers(totalPages);
            model.addAttribute(PAGE_NUMBERS, pageNumbers);
        }

        return PROFILE_SUBFOLDER_PREFIX + "profile-followers";
    }

    @GetMapping("/profiles/create-form")
    @LogExecutionTime
    public String getProfileForm(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 Model model) {
        log.info("Entering getProfileForm");

        Long userId = userDetails.getUserId();
        if (profileService.isProfileExistForUser(userId)) {
            log.info("Profile already exists for user, redirecting to /profiles");

            return "redirect:/profiles";
        }

        ProfileDto profileDto = new ProfileDto();
        model.addAttribute(PROFILE, profileDto);

        return PROFILE_SUBFOLDER_PREFIX + "profile-create-form";
    }

    @GetMapping("/profiles/{profileId}/update-form")
    @LogExecutionTime
    public String getProfileUpdateForm(@PathVariable("profileId") Long profileId,
                                       Model model) {
        log.info("Entering getProfileUpdateForm");

        Profile profile = profileService.returnProfileById(profileId);
        String profileUsername = profileService.returnProfileUsername(profileId);
        model.addAttribute(PROFILE, profile);
        model.addAttribute(PROFILE_ID, profileId);
        model.addAttribute("profileUsername", profileUsername);

        return PROFILE_SUBFOLDER_PREFIX + "profile-update-form";
    }

    /**
     * WRITE Mappings
     */
    @PostMapping("/profiles")
    @LogExecutionTime
    public String createNewProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                   HttpSession session,
                                   @ModelAttribute(PROFILE) @Valid ProfileDto profileDto,
                                   BindingResult bindingResult) {
        log.info("Entering createNewProfile");

        if (bindingResult.hasErrors()) {
            log.info("Validation errors occurred");

            return PROFILE_SUBFOLDER_PREFIX + "profile-create-form";
        }

        Long userId = userDetails.getUserId();
        profileService.createNewProfile(userId, profileDto);

        SecurityContextHolder.clearContext();
        session.invalidate();
        return "redirect:/login";
    }

    @PutMapping("/profiles/{profileId}")
    @LogExecutionTime
    public String updateExistingProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable("profileId") Long profileId,
                                        @ModelAttribute(PROFILE) @Valid ProfileDto profileDto,
                                        BindingResult bindingResult,
                                        Model model) {
        log.info("Entering updateExistingProfile");

        Long userId = userDetails.getUserId();
        if (bindingResult.hasErrors()) {
            log.info("Validation errors occurred");

            String profileUsername = profileService.returnProfileUsername(profileId);
            model.addAttribute(PROFILE_ID, profileId);
            model.addAttribute("profileUsername", profileUsername);
            return PROFILE_SUBFOLDER_PREFIX + "profile-update-form";
        }

        profileService.updateExistingProfile(userId, profileId, profileDto);

        return "redirect:/profiles/" + profileId;
    }

    @DeleteMapping("/profiles/{profileId}")
    @LogExecutionTime
    public String deleteExistingProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable("profileId") Long profileId,
                                        HttpSession session) {
        log.info("Entering deleteExistingProfile");

        Long userId = userDetails.getUserId();
        profileService.deleteExistingProfile(userId, profileId);

        SecurityContextHolder.clearContext();
        session.invalidate();
        return "redirect:/login";
    }
}
