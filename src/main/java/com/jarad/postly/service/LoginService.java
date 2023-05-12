package com.jarad.postly.service;

import org.springframework.security.core.Authentication;

public interface LoginService {

    boolean isProfileExistForUser(Authentication authentication);

}
