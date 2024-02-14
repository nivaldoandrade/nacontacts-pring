package com.nasa.nacontacts.domain.exceptions;

public class CategoryExistsException extends RuntimeException {

    public CategoryExistsException() {
        super("The category is already exists");
    }
}
