package com.jarad.postly.controller;

import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.LoginService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private final LoginService loginService;
    private final String LOGIN_SUBFOLDER_PREFIX = "login/";

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/")
    public String showIndexPage(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpSession session) {
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
    public String showLoginPage() {
        return LOGIN_SUBFOLDER_PREFIX + "login";
    }
}
