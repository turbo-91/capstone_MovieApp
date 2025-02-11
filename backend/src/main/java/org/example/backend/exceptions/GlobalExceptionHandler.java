package org.example.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DatabaseException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleDatabaseException(DatabaseException ex) {
        return Map.of("message", ex.getMessage());
    }
}
