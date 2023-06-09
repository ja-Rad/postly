package com.jarad.postly.security;

import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;

public class UserDetailsImpl implements UserDetails {
    private User user;

    @Autowired
    public UserDetailsImpl(User user) {
        this.user = user;
    }

    @Transactional
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return grantedAuthorities;
    }

    public Long getUserId() {
        return user.getId();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Default realisation of Spring Security uses username as a primary field for identity, but we use email.
     *
     * @return String email of User Entity as a primary field for the authentication name.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    public boolean isActiveProfile() {
        return user.isActiveProfile();
    }
}
