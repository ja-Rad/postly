package com.jarad.postly.security;

public enum SecurityRole {
    ROLE_USER("ROLE_USER"),
    ROLE_PROFILE_ACTIVE("ROLE_PROFILE_ACTIVE"),
    ROLE_ADMIN("ROLE_ADMIN");

    private final String role;

    SecurityRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return this.role;
    }
}
