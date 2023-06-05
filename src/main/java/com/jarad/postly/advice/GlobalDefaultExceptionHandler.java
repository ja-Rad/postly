package com.jarad.postly.advice;

import com.jarad.postly.util.exception.AuthorNotFoundException;
import com.jarad.postly.util.exception.CommentNotFoundException;
import com.jarad.postly.util.exception.EmailNotFoundException;
import com.jarad.postly.util.exception.FollowerNotFoundException;
import com.jarad.postly.util.exception.FollowerServiceException;
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
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";

    /**
     * Error Handler for status code: 403 - Forbidden
     *
     * @return error page located at templates/error.html
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({
            FollowerServiceException.class
    })
    public String errorHandler403(HttpServletRequest request, Exception ex, Model model) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        model.addAttribute(STATUS, status.value());
        model.addAttribute("url", request.getRequestURL());
        model.addAttribute(MESSAGE, ex.getMessage());

        return DEFAULT_ERROR_VIEW;
    }

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
        model.addAttribute(STATUS, status.value());
        model.addAttribute("url", request.getRequestURL());
        model.addAttribute(MESSAGE, ex.getMessage());

        return DEFAULT_ERROR_VIEW;
    }

    /**
     * Error Handler for status code: 500 - Internal Server Error
     *
     * @return error page located at templates/error.html
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({RuntimeException.class})
    public String errorHandler500(HttpServletRequest request, Exception ex, Model model) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        model.addAttribute(STATUS, status.value());
        model.addAttribute("url", request.getRequestURL());
        model.addAttribute(MESSAGE, ex.getMessage());

        return DEFAULT_ERROR_VIEW;
    }
}