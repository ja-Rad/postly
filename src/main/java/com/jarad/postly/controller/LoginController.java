package com.jarad.postly.controller;

import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.LoginService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/")
    public String showIndexPage(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpSession session) {
        Long userId = userDetails.getUserId();
        boolean profileExistForUser = loginService.isProfileExistForUser(userId);

        if (profileExistForUser) {
            session.setAttribute("usersActiveProfileId", userDetails.getUserId());
            return "redirect:/posts";
        }

        session.setAttribute("usersActiveProfileId", null);
        return "redirect:/profiles/form";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @RolesAllowed("USER")
    @GetMapping("/user")
    public String showUserPage() {
        return "user";
    }

    @RolesAllowed("ADMIN")
    @GetMapping("/admin")
    public String showAdminPage() {
        return "admin";
    }
}
