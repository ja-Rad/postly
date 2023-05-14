package com.jarad.postly.controller;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

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
    public String getPaginatedProfiles(@RequestParam(value = "page", defaultValue = "1") int page,
                                       @RequestParam(value = "size", defaultValue = "10") int size,
                                       Model model) {
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
        if (notEqual(id, userId)) {
            model.addAttribute("personalProfile", false);
        }

        Profile profile = profileService.returnProfileById(id);
        model.addAttribute("personalProfile", true);
        model.addAttribute("profile", profile);

        return PROFILE_SUBFOLDER_PREFIX + "profile";
    }

    @GetMapping("/profiles/{id}/authors")
    public String getProfileAuthors(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @PathVariable Long id) {
        Long userId = userDetails.getUserId();
        
        return PROFILE_SUBFOLDER_PREFIX + "profile-authors";
    }

    @GetMapping("/profiles/form")
    public String getProfileForm(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 Model model) {
        Long userId = userDetails.getUserId();
        boolean profileExistForUser = profileService.isProfileExistForUser(userId);
        if (profileExistForUser) {
            return "redirect:/profiles";
        }

        ProfileDto profileDto = new ProfileDto();
        model.addAttribute("profile", profileDto);

        return PROFILE_SUBFOLDER_PREFIX + "profile-form";
    }

    @GetMapping("/profiles/update-fail")
    public String getUpdateFailPage() {
        return PROFILE_SUBFOLDER_PREFIX + "update-fail";
    }

    @GetMapping("/profiles/update-success")
    public String getUpdateSuccessPage() {
        return PROFILE_SUBFOLDER_PREFIX + "update-success";
    }

    @GetMapping("/profiles/delete-fail")
    public String getDeleteFailPage() {
        return PROFILE_SUBFOLDER_PREFIX + "delete-fail";
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
                                   @ModelAttribute("profile") @Valid ProfileDto profileDto) {
        Long userId = userDetails.getUserId();
        session.setAttribute("usersActiveProfileId", userId);
        Long profileId = profileService.createNewProfileAndReturnProfileId(userId, profileDto);

        return "redirect:/profiles/" + profileId;
    }

    @PutMapping("/profiles/{id}")
    public String updateExistingProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable("id") Long id,
                                        @ModelAttribute("profile") @Valid ProfileDto profileDto) {
        Long userId = userDetails.getUserId();
        if (notEqual(id, userId)) {
            return "redirect:/profiles/update-fail";
        }

        profileService.updateExistingProfile(id, profileDto);

        return "redirect:/profiles/update-success";
    }

    @DeleteMapping("/profiles/{id}")
    public String deleteExistingProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable("id") Long id,
                                        HttpSession session) {
        Long userId = userDetails.getUserId();
        if (notEqual(id, userId)) {
            return "redirect:/profiles/delete-fail";
        }

        session.setAttribute("usersActiveProfileId", null);
        profileService.deleteExistingProfile(id);

        return "redirect:/profiles/delete-success";
    }
}
