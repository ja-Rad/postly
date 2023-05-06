package com.jarad.postly.controller;

import com.jarad.postly.service.UserServiceImpl;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.exception.EmailNotFoundException;
import com.jarad.postly.util.exception.UserAlreadyExistException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class UserController {

    private UserServiceImpl userServiceImpl;

    @Autowired
    public UserController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    /**
     * /verify ENDPOINTS
     */

    @GetMapping("/verify")
    public String showVerifyPage(@RequestParam String code) {
        boolean isVerified = userServiceImpl.verifyNewUser(code);
        if (isVerified) {
            return "verify-success";
        } else {
            return "verify-fail";
        }
    }

    @GetMapping("verify-notification")
    public String showVerifyNotificationPage() {
        return "verify-notification";
    }

    /**
     * /forgot ENDPOINTS
     */

    @GetMapping("/forgot-password-verify")
    public String showForgotPasswordVerifyPage(@RequestParam String code, Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        model.addAttribute("code", code);
        return "forgot-password-form";
    }

    @PostMapping("/forgot-password-verify")
    public String processForgotPasswordVerifyPage(@RequestParam String code,
                                                  @ModelAttribute("user") UserDto userDto) {
        boolean isVerified = userServiceImpl.verifyForgotPassword(code, userDto);
        if (isVerified) {
            return "redirect:/forgot-password-verify-success";
        } else {
            return "redirect:/forgot-password-verify-fail";
        }
    }

    @GetMapping("/forgot-password-verify-success")
    public String showForgotPasswordVerifySuccessPage() {
        return "forgot-password-verify-success";
    }

    @GetMapping("/forgot-password-verify-fail")
    public String showForgotPasswordVerifyFailPage() {
        return "forgot-password-verify-fail";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String resetPasswordForUserAccount(@ModelAttribute("user") UserDto userDto) {
        try {
            userServiceImpl.resetPasswordForExistingUser(userDto);
        } catch (EmailNotFoundException emailNotFoundException) {
            log.warn(emailNotFoundException.toString());
        }
        return "redirect:/forgot-password-verify-notification";
    }

    @GetMapping("/forgot-password-verify-notification")
    public String showForgotPasswordVerifyNotificationPage() {
        return "forgot-password-verify-notification";
    }

    /**
     * /registration ENDPOINTS
     */

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto) {
        try {
            userServiceImpl.registerNewUserAccount(userDto);
        } catch (UserAlreadyExistException userAlreadyExistException) {
            log.warn(userAlreadyExistException.toString());
        }
        return "redirect:/verify-notification";
    }
}
