package com.jarad.postly.controller;

import com.jarad.postly.service.UserService;
import com.jarad.postly.util.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;

    private final String USER_SUBFOLDER_PREFIX = "user/";

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    /**
     * READ Mappings
     */
    @GetMapping("/users/verify")
    public String showVerifyPage(@RequestParam String code) {
        boolean isVerified = userService.verifyNewUser(code);
        if (isVerified) {
            return USER_SUBFOLDER_PREFIX + "verify-success";
        } else {
            return USER_SUBFOLDER_PREFIX + "verify-fail";
        }
    }

    @GetMapping("/users/verify-notification")
    public String showVerifyNotificationPage() {
        return USER_SUBFOLDER_PREFIX + "verify-notification";
    }

    @GetMapping("/users/forgot-password-verify")
    public String showForgotPasswordVerifyPage(@RequestParam String code, Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        model.addAttribute("code", code);
        return USER_SUBFOLDER_PREFIX + "forgot-password-form";
    }

    @GetMapping("/users/forgot-password-verify-success")
    public String showForgotPasswordVerifySuccessPage() {
        return USER_SUBFOLDER_PREFIX + "forgot-password-verify-success";
    }

    @GetMapping("/users/forgot-password-verify-fail")
    public String showForgotPasswordVerifyFailPage() {
        return USER_SUBFOLDER_PREFIX + "forgot-password-verify-fail";
    }

    @GetMapping("/users/forgot-password")
    public String showForgotPasswordPage(Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return USER_SUBFOLDER_PREFIX + "forgot-password";
    }

    @GetMapping("/users/forgot-password-verify-notification")
    public String showForgotPasswordVerifyNotificationPage() {
        return USER_SUBFOLDER_PREFIX + "forgot-password-verify-notification";
    }

    @GetMapping("/users/registration")
    public String showRegistrationForm(Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return USER_SUBFOLDER_PREFIX + "registration";
    }

    /**
     * WRITE Mappings
     */
    @PostMapping("/users/forgot-password-verify")
    public String processForgotPasswordVerifyPage(@RequestParam String code,
                                                  @ModelAttribute("user") UserDto userDto) {
        boolean isVerified = userService.verifyForgotPassword(code, userDto);
        if (isVerified) {
            return "redirect:/users/forgot-password-verify-success";
        } else {
            return "redirect:/users/forgot-password-verify-fail";
        }
    }

    @PostMapping("/users/forgot-password")
    public String resetPasswordForUserAccount(@ModelAttribute("user") UserDto userDto) {
        userService.resetPasswordForExistingUser(userDto);
        return "redirect:/users/forgot-password-verify-notification";
    }

    @PostMapping("/users/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto) {
        userService.registerNewUserAccount(userDto);
        return "redirect:/users/verify-notification";
    }
}
