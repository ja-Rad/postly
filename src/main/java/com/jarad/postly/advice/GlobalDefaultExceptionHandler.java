package com.jarad.postly.advice;

import com.jarad.postly.util.exception.AuthorNotFoundException;
import com.jarad.postly.util.exception.CommentNotFoundException;
import com.jarad.postly.util.exception.EmailNotFoundException;
import com.jarad.postly.util.exception.FollowerNotFoundException;
import com.jarad.postly.util.exception.PostNotFoundException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import com.jarad.postly.util.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class GlobalDefaultExceptionHandler {

    public static final String DEFAULT_ERROR_VIEW = "error";

    /**
     * Error Handler for status code: 404 - Not Found
     *
     * @return error page located at templates/error.html
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AuthorNotFoundException.class,
            CommentNotFoundException.class,
            EmailNotFoundException.class,
            FollowerNotFoundException.class,
            PostNotFoundException.class,
            ProfileNotFoundException.class,
            UserNotFoundException.class
    })
    public String errorHandler404(HttpServletRequest request, Exception ex, Model model) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        model.addAttribute("status", status.value());
        model.addAttribute("url", request.getRequestURL());
        model.addAttribute("message", ex.getMessage());

        return DEFAULT_ERROR_VIEW;
    }
}