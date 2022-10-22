package com.zor07.notesbackend.api.v1.advice;

import com.zor07.notesbackend.exception.IllegalAuthorizationHeaderException;
import com.zor07.notesbackend.exception.IllegalResourceAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;

@ControllerAdvice
public class RestControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestControllerAdvice.class);

    @ResponseStatus(value = HttpStatus.NO_CONTENT, reason = "Resource is not found")
    @ExceptionHandler(EntityNotFoundException.class)
    public void handleNotFound(Throwable t) {
        LOGGER.error("", t);
        // Nothing to do
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, IllegalResourceAccessException.class})
    public void handleBadRequest(Throwable t) {
        LOGGER.error("", t);
        // Nothing to do
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IllegalAuthorizationHeaderException.class)
    public void handleForbidden(Throwable t) {
        LOGGER.error("", t);
        // Nothing to do
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IOException.class)
    public void handleServerError(Throwable t) {
        LOGGER.error("", t);
        // Nothing to do
    }
}
