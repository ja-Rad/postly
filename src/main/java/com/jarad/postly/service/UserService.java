package com.jarad.postly.service;

import com.jarad.postly.entity.User;
import com.jarad.postly.util.dto.UserDto;

public interface UserService {
    void registerNewUserAccount(UserDto userDto);

    void resetPasswordForExistingUser(UserDto userDto);

    void sendVerificationEmail(User user);

    void sendForgotPasswordEmail(User user);

    boolean verifyNewUser(String code);

    boolean verifyForgotPassword(String code, UserDto userDto);
}
