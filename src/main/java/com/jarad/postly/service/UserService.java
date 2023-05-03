package com.jarad.postly.service;

import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.repository.RoleRepository;
import com.jarad.postly.repository.UserRepository;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.enums.Roles;
import com.jarad.postly.util.exception.UserAlreadyExistException;
import com.jarad.postly.util.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Service
public class UserService implements IUserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: " + userDto.getEmail());
        }

        Optional<Role> userRole = roleRepository.findByName(Roles.USER_ROLE.toString());
        if (userRole.isEmpty()) {
            Role buildUserRole = Role.builder().name(Roles.USER_ROLE.toString()).build();
            roleRepository.save(buildUserRole);

            userRole = roleRepository.findByName(Roles.USER_ROLE.toString());
        }

        User user = userMapper.mapToEntity(userDto);
        user.setRoles(Stream.of(userRole.get()).collect(toSet()));

        return userRepository.save(user);
    }
}
