package com.nasa.nacontacts.domain.interfaces;

import com.nasa.nacontacts.domain.validators.FileTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileType {

    String message() default "The file is not accepted";
    String[] allowedExtensions();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
