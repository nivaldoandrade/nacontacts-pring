package com.nasa.nacontacts.domain.exceptions;

public class FileTypeValidationException extends RuntimeException {
    public FileTypeValidationException(String message) {
        super(message);
    }
}
