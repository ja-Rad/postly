package com.jarad.postly.service;

import com.jarad.postly.entity.User;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.dto.UserDtoOnlyEmail;
import com.jarad.postly.util.dto.UserDtoOnlyPassword;
import org.springframework.mail.javamail.MimeMessageHelper;

public interface UserService {
    void registerNewUserAccount(UserDto userDto);

    void resetPasswordForExistingUser(UserDtoOnlyEmail userDto);

    void sendVerificationEmail(User user);

    void sendForgotPasswordEmail(User user);

    void createVerifyEmailTemplate(User user, MimeMessageHelper helper);

    void createForgotPasswordEmailTemplate(User user, MimeMessageHelper helper);

    boolean verifyNewUser(String code);

    boolean verifyForgotPassword(String code, UserDtoOnlyPassword userDto);
}
