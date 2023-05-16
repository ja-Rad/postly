package com.jarad.postly.util.exception;

public class ProfileNotAllowedToUpdateThisPost extends RuntimeException {
    public ProfileNotAllowedToUpdateThisPost(String message) {
        super(message);
    }
}
