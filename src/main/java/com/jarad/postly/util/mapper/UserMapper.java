package com.jarad.postly.util.mapper;

import com.jarad.postly.entity.User;
import com.jarad.postly.util.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements IBasicMapper<User, UserDto> {

    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User mapToEntity(UserDto userDto) {
        return User.builder()
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();
    }

    @Override
    public UserDto mapToDto(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .build();
    }
}
