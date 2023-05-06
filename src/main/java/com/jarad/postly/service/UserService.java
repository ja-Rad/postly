package com.jarad.postly.service;

import com.jarad.postly.entity.User;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.exception.EmailNotFoundException;
import com.jarad.postly.util.exception.UserAlreadyExistException;

public interface UserService {
    void registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException;

    void resetPasswordForExistingUser(UserDto userDto) throws EmailNotFoundException;

    void sendVerificationEmail(User user);

    void sendForgotPasswordEmail(User user);
}
