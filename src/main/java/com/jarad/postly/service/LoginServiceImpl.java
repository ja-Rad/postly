package com.jarad.postly.service;

import com.jarad.postly.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    private final ProfileRepository profileRepository;

    @Autowired
    public LoginServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public boolean isProfileExistForUser(Long id) {
        return profileRepository.existsByUser_Id(id);
    }
}
