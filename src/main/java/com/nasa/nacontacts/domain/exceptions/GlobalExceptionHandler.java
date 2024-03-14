package com.nasa.nacontacts.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(StorageNotFoundException.class)
    public ResponseEntity<RestErrorResponse> handleUncaught(StorageNotFoundException e) {
        int statusCode = HttpStatus.NOT_FOUND.value();

        RestErrorResponse error = new RestErrorResponse(
                statusCode,
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(statusCode).body(error);
    }

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
    public ResponseEntity<RestErrorResponseWithFieldErrors> validation(MethodArgumentNotValidException e) {
        int statusCode = HttpStatus.BAD_REQUEST.value();

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(ex -> new FieldError(ex.getField(), ex.getDefaultMessage())).toList();

        RestErrorResponseWithFieldErrors error = new RestErrorResponseWithFieldErrors(
                statusCode,
                fieldErrors,
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        int statusCode = HttpStatus.BAD_REQUEST.value();

        String message = String.format("The parameter %s is invalid", e.getName());

        RestErrorResponse error = new RestErrorResponse(
                statusCode,
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.status(statusCode).body(error);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<RestErrorResponse> handleEmailAlreadyInUse(EmailAlreadyInUseException e) {
        int statusCode = HttpStatus.BAD_REQUEST.value();

        RestErrorResponse error = new RestErrorResponse(
                statusCode,
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(statusCode).body(error);
    }

    @ExceptionHandler(FileTypeValidationException.class)
    public ResponseEntity<RestErrorResponse> handleFileTypeValidation(FileTypeValidationException e) {
        int statusCode = HttpStatus.BAD_REQUEST.value();

        RestErrorResponse error = new RestErrorResponse(
                statusCode,
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(statusCode).body(error);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<RestErrorResponse> handleFileStorageException(FileStorageException e) {
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

        RestErrorResponse error = new RestErrorResponse(
                statusCode,
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(statusCode).body(error);
    }
};
