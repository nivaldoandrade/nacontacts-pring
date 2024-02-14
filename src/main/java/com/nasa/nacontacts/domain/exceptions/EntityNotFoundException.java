package com.nasa.nacontacts.domain.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

//@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends  RuntimeException {

    @Getter
    private final UUID id;
    private final Class<?> theClass;

    public EntityNotFoundException(UUID id, Class<?> theClass) {
        super(String.format("%s with id = %s not found", theClass.getSimpleName(), id));
        this.id = id;
        this.theClass = theClass;
    }

    public String getTheClassName() {
        return theClass.getSimpleName();
    }
}
