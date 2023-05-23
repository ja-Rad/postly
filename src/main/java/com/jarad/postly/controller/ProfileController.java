package com.jarad.postly.controller;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.ProfileService;
import com.jarad.postly.util.dto.ProfileDto;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
import java.util.Objects;
import java.util.Set;

@Controller
public class ProfileController {

    private final ProfileService profileService;
    private final String PROFILE_SUBFOLDER_PREFIX = "profile/";

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * READ Mappings
     */
    @GetMapping("/profiles")
    public String getPaginatedProfiles(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                       @RequestParam(value = "size", defaultValue = "10") int size,
                                       Model model) {
        Long userId = userDetails.getUserId();
        model.addAttribute("userId", userId);

        Set<Long> authorsByUserId = profileService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Page<Profile> profilePage = profileService.returnPaginatedProfilesByCreationDateDescending(page - 1, size);
        int totalPages = profilePage.getTotalPages();

        if (totalPages > 0) {
            List<Integer> pageNumbers = profileService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("profilePage", profilePage);

        return PROFILE_SUBFOLDER_PREFIX + "profiles";
    }

    @GetMapping("/profiles/{id}")
    public String getProfileById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable Long id,
                                 Model model) {
        Long userId = userDetails.getUserId();

        Set<Long> authorsByUserId = profileService.returnAuthorsByUserId(userId);
        model.addAttribute("authorsByUserId", authorsByUserId);

        Profile profile = profileService.returnProfileById(id);
        model.addAttribute("personalProfile", false);
        model.addAttribute("profile", profile);

        if (Objects.equals(id, userId)) {
            model.addAttribute("personalProfile", true);
        }

        return PROFILE_SUBFOLDER_PREFIX + "profile";
    }

    @GetMapping("/profiles/{id}/posts")
    public String getProfilePosts(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                  @PathVariable Long id,
                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                  Model model) {
        Long userId = userDetails.getUserId();
        model.addAttribute("profileId", id);
        model.addAttribute("personalProfile", false);
        if (Objects.equals(id, userId)) {
            model.addAttribute("personalProfile", true);
        }

        Page<Post> postPage = profileService.returnProfilePaginatedPostsByCreationDateDescending(id, page - 1, size);
        int totalPages = postPage.getTotalPages();
        model.addAttribute("postPage", postPage);
        if (totalPages > 0) {
            List<Integer> pageNumbers = profileService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return PROFILE_SUBFOLDER_PREFIX + "profile-posts";
    }

    @GetMapping("/profiles/{id}/authors")
    public String getProfileAuthors(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @PathVariable Long id,
                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    Model model) {
        Long userId = userDetails.getUserId();
        model.addAttribute("profileId", id);
        model.addAttribute("personalProfile", false);
        if (Objects.equals(id, userId)) {
            model.addAttribute("personalProfile", true);
        }

        Page<Follower> authorPage = profileService.returnProfilePaginatedAuthorsByCreationDateDescending(id, page - 1, size);
        int totalPages = authorPage.getTotalPages();
        model.addAttribute("authorPage", authorPage);
        if (totalPages > 0) {
            List<Integer> pageNumbers = profileService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return PROFILE_SUBFOLDER_PREFIX + "profile-authors";
    }

    @GetMapping("/profiles/{id}/followers")
    public String getProfileFollowers(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @PathVariable Long id,
                                      @RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "size", defaultValue = "10") int size,
                                      Model model) {
        Long userId = userDetails.getUserId();
        model.addAttribute("profileId", id);
        model.addAttribute("personalProfile", false);
        if (Objects.equals(id, userId)) {
            model.addAttribute("personalProfile", true);
        }

        Page<Follower> followerPage = profileService.returnProfilePaginatedFollowersByCreationDateDescending(id, page - 1, size);
        int totalPages = followerPage.getTotalPages();
        model.addAttribute("followerPage", followerPage);
        if (totalPages > 0) {
            List<Integer> pageNumbers = profileService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return PROFILE_SUBFOLDER_PREFIX + "profile-followers";
    }

    @GetMapping("/profiles/{id}/comments")
    public String getProfileComments(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @PathVariable Long id,
                                     @RequestParam(value = "page", defaultValue = "1") int page,
                                     @RequestParam(value = "size", defaultValue = "10") int size,
                                     Model model) {
        Long userId = userDetails.getUserId();
        model.addAttribute("profileId", id);
        model.addAttribute("personalProfile", false);
        if (Objects.equals(id, userId)) {
            model.addAttribute("personalProfile", true);
        }

        Page<Comment> commentPage = profileService.returnProfilePaginatedCommentsByCreationDateDescending(id, page - 1, size);
        int totalPages = commentPage.getTotalPages();
        model.addAttribute("commentPage", commentPage);
        if (totalPages > 0) {
            List<Integer> pageNumbers = profileService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return PROFILE_SUBFOLDER_PREFIX + "profile-comments";
    }

    @GetMapping("/profiles/create-form")
    public String getProfileForm(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 Model model) {
        Long userId = userDetails.getUserId();
        boolean profileExistForUser = profileService.isProfileExistForUser(userId);
        if (profileExistForUser) {
            return "redirect:/profiles";
        }

        ProfileDto profileDto = new ProfileDto();
        model.addAttribute("profile", profileDto);

        return PROFILE_SUBFOLDER_PREFIX + "profile-create-form";
    }

    @GetMapping("/profiles/{id}/update-form")
    public String getProfileUpdateForm(@PathVariable Long id,
                                       Model model) {
        Profile profile = profileService.returnProfileById(id);
        model.addAttribute("profile", profile);
        model.addAttribute("profileId", id);

        return PROFILE_SUBFOLDER_PREFIX + "profile-update-form";
    }

    @GetMapping("/profiles/delete-success")
    public String getDeleteSuccessPage() {
        return PROFILE_SUBFOLDER_PREFIX + "delete-success";
    }

    /**
     * WRITE Mappings
     */
    @PostMapping("/profiles")
    public String createNewProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                   HttpSession session,
                                   @ModelAttribute("profile") @Valid ProfileDto profileDto,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return PROFILE_SUBFOLDER_PREFIX + "profile-create-form";
        }

        Long userId = userDetails.getUserId();
        session.setAttribute("usersActiveProfileId", userId);
        Long profileId = profileService.createNewProfileAndReturnProfileId(userId, profileDto);

        return "redirect:/profiles/" + profileId;
    }

    @PutMapping("/profiles/{id}")
    public String updateExistingProfile(@PathVariable("id") Long id,
                                        @ModelAttribute("profile") @Valid ProfileDto profileDto,
                                        BindingResult bindingResult,
                                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("profileId", id);
            return PROFILE_SUBFOLDER_PREFIX + "profile-update-form";
        }

        Long profileId = profileService.updateExistingProfile(id, profileDto);

        return "redirect:/profiles/" + profileId;
    }

    @DeleteMapping("/profiles/{id}")
    public String deleteExistingProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable("id") Long id,
                                        HttpSession session) {
        Long userId = userDetails.getUserId();
        if (Objects.equals(userId, id)) {
            session.setAttribute("usersActiveProfileId", null);
            profileService.deleteExistingProfile(id);
        }

        return "redirect:/profiles/create-form";
    }
}
