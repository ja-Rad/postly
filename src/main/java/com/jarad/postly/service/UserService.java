package com.jarad.postly.service;

import com.jarad.postly.entity.User;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.exception.UserAlreadyExistException;

public interface UserService {
    User registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException;
}
