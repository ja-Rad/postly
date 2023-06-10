package com.jarad.postly.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class SecurityController {
    public static final String DEFAULT_ERROR_VIEW = "error";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";

    @GetMapping("/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String returnAccessDeniedPage(HttpServletRequest request, Model model) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        model.addAttribute(STATUS, status.value());
        model.addAttribute("url", request.getRequestURL());
        model.addAttribute(MESSAGE, "Access Denied");
        return DEFAULT_ERROR_VIEW;
    }
}
