package com.jarad.postly.util.exception;

public class PasswordMatchesValidatorException extends RuntimeException {
    public PasswordMatchesValidatorException(String message) {
        super(message);
    }
}
