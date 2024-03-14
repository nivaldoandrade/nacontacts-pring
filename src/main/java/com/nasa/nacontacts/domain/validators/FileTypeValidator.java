package com.nasa.nacontacts.domain.validators;

import com.nasa.nacontacts.domain.exceptions.FileTypeValidationException;
import com.nasa.nacontacts.domain.interfaces.FileType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileTypeValidator implements ConstraintValidator<FileType, MultipartFile> {

    private List<String> allowedExtensions;


    @Override
    public void initialize(FileType constraintAnnotation) {
        this.allowedExtensions = List.of(constraintAnnotation.allowedExtensions());
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
       if(file == null || file.isEmpty()) {
           return true;
       }

       String originalFileName = file.getOriginalFilename();
       String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase() : null;

       if(!allowedExtensions.contains(extension)) {
           throw new FileTypeValidationException("The file type is not accepted");
       }

       return allowedExtensions.contains(extension);
    }
}
