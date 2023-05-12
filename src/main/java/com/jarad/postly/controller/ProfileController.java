package com.jarad.postly.controller;

import com.jarad.postly.entity.Profile;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.ProfileService;
import com.jarad.postly.util.dto.ProfileDto;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
    public String getPaginatedProfiles(@RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "10") int size,
                                       Model model) {
        Page<Profile> profilePage = profileService.returnPaginatedProfilesByCreationDateDescending(page, size);
        int totalPages = profilePage.getTotalPages();

        if (totalPages > 0) {
            List<Integer> pageNumbers = profileService.returnListOfPageNumbers(totalPages);
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("profilePage", profilePage);

        return PROFILE_SUBFOLDER_PREFIX + "profiles";
    }

    @GetMapping("/profiles/{id}")
    public String getOneProfileById(@PathVariable long id, Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails.getUserId().equals(id)) {
            model.addAttribute("personalProfile", true);
        }
        Profile profile = profileService.returnProfileById(id);
        model.addAttribute("profile", profile);
        return PROFILE_SUBFOLDER_PREFIX + "profile";
    }

    @GetMapping("/profiles/form")
    public String getProfileForm(Authentication authentication, Model model) {
        boolean profileExistForUser = profileService.isProfileExistForUser(authentication);
        if (profileExistForUser) {
            return "redirect:/profiles";
        }
        ProfileDto profileDto = new ProfileDto();
        model.addAttribute("profile", profileDto);
        return PROFILE_SUBFOLDER_PREFIX + "profile-form";
    }

    /**
     * WRITE Mappings
     */
    @PostMapping("/profiles")
    public String createNewProfile(Authentication authentication, @ModelAttribute("profile") @Valid ProfileDto profileDto,
                                   HttpSession session, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        session.setAttribute("usersActiveProfileId", userDetails.getUserId());
        Long profileId = profileService.createNewProfileAndReturnProfileId(authentication, profileDto);
        return "redirect:/profiles/" + profileId;
    }

    @PatchMapping("/profiles")
    public String updateExistingProfile(Authentication authentication, @ModelAttribute("profile") @Valid ProfileDto profileDto) {
        Long profileId = profileService.updateExistingProfileAndReturnProfileId(authentication, profileDto);
        return "redirect:/profiles/" + profileId;
    }

    @DeleteMapping("/profiles")
    public String deleteExistingProfile(Authentication authentication, HttpSession session) {
        session.setAttribute("usersActiveProfileId", null);
        profileService.deleteExistingProfile(authentication);
        return "redirect:/profiles";
    }
}
