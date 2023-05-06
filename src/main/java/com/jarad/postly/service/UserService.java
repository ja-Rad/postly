package com.jarad.postly.service;

import com.jarad.postly.entity.User;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.exception.UserAlreadyExistException;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface UserService {
    void registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException;

    void sendVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException;
}
