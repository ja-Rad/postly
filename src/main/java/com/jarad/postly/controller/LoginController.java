package com.jarad.postly.controller;

import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.LoginService;
import com.jarad.postly.util.annotation.LogExecutionTime;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class LoginController {

    private final LoginService loginService;
    private final String LOGIN_SUBFOLDER_PREFIX = "login/";

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/")
    @LogExecutionTime
    public String showIndexPage(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpSession session) {
        log.info("Entering showIndexPage");

        Long userId = userDetails.getUserId();
        boolean profileExistForUser = loginService.isProfileExistForUser(userId);

        if (profileExistForUser) {
            session.setAttribute("usersActiveProfileId", userId);
            return "redirect:/posts";
        }

        session.setAttribute("usersActiveProfileId", null);
        return "redirect:/profiles/create-form";
    }

    @GetMapping("/login")
    @LogExecutionTime
    public String showLoginPage() {
        log.info("Entering showLoginPage");

        return LOGIN_SUBFOLDER_PREFIX + "login";
    }
}
