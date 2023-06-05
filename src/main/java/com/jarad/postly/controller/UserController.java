package com.jarad.postly.controller;

import com.jarad.postly.service.UserService;
import com.jarad.postly.util.annotation.LogExecutionTime;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.dto.UserDtoOnlyEmail;
import com.jarad.postly.util.dto.UserDtoOnlyPassword;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class UserController {

    public static final String VALIDATION_ERRORS_OCCURRED = "Validation errors occurred";
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
    @LogExecutionTime
    public String showVerifyPage(@RequestParam String code) {
        log.info("Entering showVerifyPage");

        boolean isVerified = userService.verifyNewUser(code);
        if (!isVerified) {
            log.info("User verification failed");

            return USER_SUBFOLDER_PREFIX + "verify-fail";
        }

        log.info("User verification succeeded");
        return USER_SUBFOLDER_PREFIX + "verify-success";
    }

    @GetMapping("/users/verify-notification")
    @LogExecutionTime
    public String showVerifyNotificationPage() {
        log.info("Entering showVerifyNotificationPage");

        return USER_SUBFOLDER_PREFIX + "verify-notification";
    }

    @GetMapping("/users/forgot-password-verify")
    @LogExecutionTime
    public String showForgotPasswordVerifyPage(@RequestParam String code, Model model) {
        log.info("Entering showForgotPasswordVerifyPage");

        UserDtoOnlyPassword userDto = new UserDtoOnlyPassword();
        model.addAttribute("user", userDto);
        model.addAttribute("code", code);

        return USER_SUBFOLDER_PREFIX + "forgot-password-form";
    }

    @GetMapping("/users/forgot-password-verify-success")
    @LogExecutionTime
    public String showForgotPasswordVerifySuccessPage() {
        log.info("Entering showForgotPasswordVerifySuccessPage");

        return USER_SUBFOLDER_PREFIX + "forgot-password-verify-success";
    }

    @GetMapping("/users/forgot-password-verify-fail")
    @LogExecutionTime
    public String showForgotPasswordVerifyFailPage() {
        log.info("Entering showForgotPasswordVerifyFailPage");

        return USER_SUBFOLDER_PREFIX + "forgot-password-verify-fail";
    }

    @GetMapping("/users/forgot-password")
    @LogExecutionTime
    public String showForgotPasswordPage(Model model) {
        log.info("Entering showForgotPasswordPage");

        UserDtoOnlyEmail userDto = new UserDtoOnlyEmail();
        model.addAttribute("user", userDto);

        return USER_SUBFOLDER_PREFIX + "forgot-password";
    }

    @GetMapping("/users/forgot-password-verify-notification")
    @LogExecutionTime
    public String showForgotPasswordVerifyNotificationPage() {
        log.info("Entering showForgotPasswordVerifyNotificationPage");

        return USER_SUBFOLDER_PREFIX + "forgot-password-verify-notification";
    }

    @GetMapping("/users/registration")
    @LogExecutionTime
    public String showRegistrationForm(Model model) {
        log.info("Entering showRegistrationForm");

        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);

        return USER_SUBFOLDER_PREFIX + "registration";
    }

    /**
     * WRITE Mappings
     */
    @PostMapping("/users/forgot-password-verify")
    @LogExecutionTime
    public String processForgotPasswordVerifyPage(@RequestParam String code,
                                                  @ModelAttribute("user") @Valid UserDtoOnlyPassword userDto,
                                                  BindingResult bindingResult,
                                                  Model model) {
        log.info("Entering processForgotPasswordVerifyPage");

        if (bindingResult.hasErrors()) {
            log.info(VALIDATION_ERRORS_OCCURRED);

            model.addAttribute("code", code);
            return USER_SUBFOLDER_PREFIX + "forgot-password-form";
        }

        if (userService.verifyForgotPassword(code, userDto)) {
            log.info("Forgot password verification succeeded");

            return "redirect:/users/forgot-password-verify-success";

        } else {
            log.info("Forgot password verification failed");

            return "redirect:/users/forgot-password-verify-fail";
        }
    }

    @PostMapping("/users/forgot-password")
    @LogExecutionTime
    public String resetPasswordForUserAccount(@ModelAttribute("user") @Valid UserDtoOnlyEmail userDto,
                                              BindingResult bindingResult) {
        log.info("Entering resetPasswordForUserAccount");

        if (bindingResult.hasErrors()) {
            log.info(VALIDATION_ERRORS_OCCURRED);

            return USER_SUBFOLDER_PREFIX + "forgot-password";
        }
        userService.resetPasswordForExistingUser(userDto);
        log.info("Password reset for existing user");

        return "redirect:/users/forgot-password-verify-notification";
    }

    @PostMapping("/users/registration")
    @LogExecutionTime
    public String registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto,
                                      BindingResult bindingResult) {
        log.info("Entering registerUserAccount");

        if (bindingResult.hasErrors()) {
            log.info(VALIDATION_ERRORS_OCCURRED);

            return USER_SUBFOLDER_PREFIX + "registration";
        }

        userService.registerNewUserAccount(userDto);
        log.info("New user account registered");

        return "redirect:/users/verify-notification";
    }
}
