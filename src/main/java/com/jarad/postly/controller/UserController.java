package com.jarad.postly.controller;

import com.jarad.postly.entity.User;
import com.jarad.postly.service.UserServiceImpl;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.exception.UserAlreadyExistException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Controller
public class UserController {

    private UserServiceImpl userServiceImpl;

    @Autowired
    public UserController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping("/registration")
    public String showRegistrationForm(WebRequest request, Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto,
                                      HttpServletRequest request,
                                      Errors errors) {

        try {
            User registered = userServiceImpl.registerNewUserAccount(userDto);
        } catch (UserAlreadyExistException userAlreadyExistException) {
            log.warn(userAlreadyExistException.toString());
        }
        return "redirect:/login";
    }
}
