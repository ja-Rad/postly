package com.jarad.postly.util.exception;

public class ProfileForUserAlreadyExistException extends RuntimeException {
    public ProfileForUserAlreadyExistException(String message) {
        super(message);
    }
}
