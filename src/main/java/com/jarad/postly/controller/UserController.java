package com.jarad.postly.controller;

import com.jarad.postly.service.UserServiceImpl;
import com.jarad.postly.util.dto.UserDto;
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

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "registration";
    }

    @GetMapping(value = "/verify")
    public String showVerifyPage(@RequestParam String code) {
        boolean isVerified = userServiceImpl.verify(code);
        if (isVerified) {
            return "verify-success";
        } else {
            return "verify-fail";
        }
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto) {
        try {
            userServiceImpl.registerNewUserAccount(userDto);
        } catch (UserAlreadyExistException userAlreadyExistException) {
            log.warn(userAlreadyExistException.toString());
        }
        return "redirect:/login";
    }
}
