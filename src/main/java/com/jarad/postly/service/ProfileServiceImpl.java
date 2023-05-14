package com.jarad.postly.service;

import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.ProfileFollower;
import com.jarad.postly.entity.User;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.repository.UserRepository;
import com.jarad.postly.util.dto.ProfileDto;
import com.jarad.postly.util.exception.ProfileForUserAlreadyExistException;
import com.jarad.postly.util.exception.ProfileNotFountException;
import com.jarad.postly.util.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Service
public class ProfileServiceImpl implements ProfileService {

    public static final int PAGE_SIZE = 10;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    public Page<Profile> returnPaginatedProfilesByCreationDateDescending(int page, int size) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("creationDate").descending());
        return profileRepository.findAll(pageable);
    }

    public List<Integer> returnListOfPageNumbers(int totalPages) {
        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(toList());
    }

    @Override
    public Profile returnProfileById(Long id) {
        Optional<Profile> optionalProfile = profileRepository.findById(id);
        if (optionalProfile.isEmpty()) {
            throw new ProfileNotFountException("Profile with this id: " + id + " doesn`t exist");
        }

        return optionalProfile.get();
    }

    @Override
    public ProfileFollower returnAuthorsByProfileId(Long id) {
        return null;
    }

    public Long createNewProfileAndReturnProfileId(Long id, ProfileDto profileDto) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User with id: " + id + " doesn`t exist");
        }
        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(id);
        if (optionalProfile.isPresent()) {
            throw new ProfileForUserAlreadyExistException("Profile for: " + id + " already exist");
        }

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

    public void updateExistingProfile(Long id, ProfileDto profileDto) {
        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(id);
        if (optionalProfile.isEmpty()) {
            throw new ProfileNotFountException("Profile for user with id: " + id + " doesn`t exist");
        }

        Profile profile = optionalProfile.get();
        profile.setUsername(profileDto.getUsername());

        profileRepository.save(profile);
    }

    public void deleteExistingProfile(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User with id: " + id + " doesn`t exist");
        }
        Optional<Profile> optionalProfile = profileRepository.findByUser_Id(id);
        if (optionalProfile.isEmpty()) {
            throw new ProfileNotFountException("Profile for user with id: " + id + " doesn`t exist");
        }

        User user = optionalUser.get();
        user.setActiveProfile(false);
        Profile profile = optionalProfile.get();
        profile.setUser(user);

        profileRepository.delete(profile);
    }

    public boolean isProfileExistForUser(Long id) {
        return profileRepository.existsByUser_Id(id);
    }
}
