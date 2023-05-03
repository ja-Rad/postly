package com.jarad.postly.controller;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

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
