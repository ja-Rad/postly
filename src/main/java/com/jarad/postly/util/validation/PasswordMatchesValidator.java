package com.jarad.postly.util.validation;

import com.jarad.postly.util.annotation.PasswordMatches;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.dto.UserDtoOnlyPassword;
import com.jarad.postly.util.exception.PasswordMatchesValidatorException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object instanceof UserDto user) {
            return user.getPassword().equals(user.getMatchingPassword());
        }

        if (object instanceof UserDtoOnlyPassword user) {
            return user.getPassword().equals(user.getMatchingPassword());
        }

        throw new PasswordMatchesValidatorException("Provided object is not an instance of UserDto or UserDtoOnlyPassword. Current class of the object is: " + object.getClass());
    }
}