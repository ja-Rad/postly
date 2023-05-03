package com.jarad.postly.util.enums;

public enum Roles {
    USER_ROLE("ROLE_USER"),
    ADMIN_ROLE("ROLE_ADMIN");

    private final String role;

    Roles(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return this.role;
    }
}
