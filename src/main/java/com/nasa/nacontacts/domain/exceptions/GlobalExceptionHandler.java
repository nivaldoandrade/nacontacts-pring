package com.nasa.nacontacts.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<RestErrorResponse> notFound(EntityNotFoundException e) {
        int  statusCode = HttpStatus.NOT_FOUND.value();

        RestErrorResponse error = new RestErrorResponse(
                statusCode,
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(statusCode).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestErrorResponse> validation(MethodArgumentNotValidException e) {
        int statusCode = HttpStatus.BAD_REQUEST.value();

        RestErrorResponse error = new RestErrorResponse(
                statusCode,
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(statusCode).body(error);
    }

    @ExceptionHandler(CategoryExistsException.class)
    public ResponseEntity<RestErrorResponse> categoryExists(CategoryExistsException e) {
        int statusCode = HttpStatus.BAD_REQUEST.value();

        RestErrorResponse error = new RestErrorResponse(
                statusCode,
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(statusCode).body(error);
    }
}
