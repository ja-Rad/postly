package com.jarad.postly.service;

import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.User;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.repository.UserRepository;
import com.jarad.postly.util.dto.ProfileDto;
import com.jarad.postly.util.exception.ProfileForUserAlreadyExistException;
import com.jarad.postly.util.exception.ProfileNotFountException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    public Page<Profile> returnPaginatedProfilesByCreationDateDescending(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("creationDate").descending());
        return profileRepository.findAll(pageable);
    }

    public List<Integer> returnListOfPageNumbers(int totalPages) {
        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(toList());
    }

    @Override
    public Profile returnProfileById(long id) {
        Optional<Profile> optionalProfile = profileRepository.findById(id);
        if (optionalProfile.isPresent()) {
            return optionalProfile.get();
        }

        throw new ProfileNotFountException("Profile with this id: " + id + " doesn`t exist");
    }

    public Long createNewProfileAndReturnProfileId(Authentication authentication, ProfileDto profileDto) {
        String authenticatedUserEmail = authentication.getName();
        Optional<Profile> optionalProfile = profileRepository.findByUser_Email(authenticatedUserEmail);
        if (optionalProfile.isPresent()) {
            throw new ProfileForUserAlreadyExistException("Profile for: " + authenticatedUserEmail + " already exist");
        }

        Optional<User> optionalUser = userRepository.findByEmail(authenticatedUserEmail);
        User user = optionalUser.get();
        user.setActiveProfile(true);

        Profile profile = Profile.builder()
                .username(profileDto.getUsername())
                .creationDate(Instant.now())
                .user(user)
                .build();

        Profile savedProfile = profileRepository.save(profile);

        return savedProfile.getId();
    }

    public Long updateExistingProfileAndReturnProfileId(Authentication authentication, ProfileDto profileDto) {
        String authenticatedUserEmail = authentication.getName();
        Optional<Profile> optionalProfile = profileRepository.findByUser_Email(authenticatedUserEmail);
        if (optionalProfile.isPresent()) {
            Profile profile = optionalProfile.get();
            profile.setUsername(profileDto.getUsername());
            Profile savedProfile = profileRepository.save(profile);

            return savedProfile.getId();
        }

        throw new ProfileNotFountException("Profile for: " + authenticatedUserEmail + " doesn`t exist");
    }

    public void deleteExistingProfile(Authentication authentication) {
        String authenticatedUserEmail = authentication.getName();
        Optional<Profile> optionalProfile = profileRepository.findByUser_Email(authenticatedUserEmail);
        if (optionalProfile.isPresent()) {
            Optional<User> optionalUser = userRepository.findByEmail(authenticatedUserEmail);
            User user = optionalUser.get();
            user.setActiveProfile(false);

            Profile profile = optionalProfile.get();
            profile.setUser(user);
            profileRepository.delete(profile);
        } else {
            throw new ProfileNotFountException("Profile for: " + authentication.getName() + " doesn`t exist");
        }
    }

    public boolean isProfileExistForUser(Authentication authentication) {
        String authenticatedUserEmail = authentication.getName();
        Optional<Profile> optionalProfile = profileRepository.findByUser_Email(authenticatedUserEmail);
        return optionalProfile.isPresent();
    }
}
