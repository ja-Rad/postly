package com.jarad.postly.util.enums;

public enum SecurityRole {
    USER_ROLE("ROLE_USER"),
    ADMIN_ROLE("ROLE_ADMIN");

    private final String role;

    SecurityRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return this.role;
    }
}
