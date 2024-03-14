package com.nasa.nacontacts.domain.services;

import com.nasa.nacontacts.domain.config.FileStorageConfig;
import com.nasa.nacontacts.domain.exceptions.FileStorageException;
import com.nasa.nacontacts.domain.exceptions.StorageNotFoundException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Transactional
public class FileUploadService {

    private final Path fileStorageLocation;

    public FileUploadService(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e) {
            throw new FileStorageException("Error creating folder where files will be stored");
        }
    }

    public ByteArrayResource getImage(String imageName) {

       try {
           Path targetLocation = this.fileStorageLocation.resolve(imageName);

           if (!Files.exists(targetLocation)) {
               throw new StorageNotFoundException("The file is not found.");
           }

           byte[] imageBytes = Files.readAllBytes(targetLocation);

           ByteArrayResource resource = new ByteArrayResource(imageBytes);

           return resource;
       } catch (IOException e) {
           throw new FileStorageException("the file cannot be recovered.", e);
       }
    }

    public void saveFile(MultipartFile file, String filename) {
        try {
            Path targetLocation = this.fileStorageLocation.resolve(filename);

            file.transferTo(targetLocation);
        } catch(IOException e) {
            throw new FileStorageException("Error storing file "
                    + file.getOriginalFilename()
                    + ", please try again"
            );
        }
    }

    public String generateFileName(String originalFilename) {
       return UUID.randomUUID().toString() + "_" + originalFilename;
    }
}
