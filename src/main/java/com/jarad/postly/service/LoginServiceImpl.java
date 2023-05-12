package com.jarad.postly.service;

import com.jarad.postly.entity.Profile;
import com.jarad.postly.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginServiceImpl implements LoginService {

    private final ProfileRepository profileRepository;

    @Autowired
    public LoginServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public boolean isProfileExistForUser(Authentication authentication) {
        String authenticationName = authentication.getName();
        Optional<Profile> optionalProfile = profileRepository.findByUser_Email(authenticationName);
        return optionalProfile.isPresent();
    }
}
