package com.jarad.postly.util.mapper;

import com.jarad.postly.entity.User;
import com.jarad.postly.util.dto.UserDto;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements EntityDtoMapper<User, UserDto> {

    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserMapperImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User mapToEntity(UserDto userDto) {
        return User.builder()
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .verificationCode(RandomString.make(64))
                .build();
    }

    @Override
    public UserDto mapToDto(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .build();
    }
}