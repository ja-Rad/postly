package com.jarad.postly.util.exception;

public class FollowerNotFoundException extends RuntimeException {
    public FollowerNotFoundException(String message) {
        super(message);
    }
}
