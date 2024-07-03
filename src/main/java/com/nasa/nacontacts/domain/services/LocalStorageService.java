package com.nasa.nacontacts.domain.services;

import com.nasa.nacontacts.domain.config.StorageProperties;
import com.nasa.nacontacts.domain.exceptions.FileStorageException;
import com.nasa.nacontacts.domain.exceptions.StorageNotFoundException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalStorageService implements StorageService {

    private final Path localStorageLocation;

    public LocalStorageService(StorageProperties storageProperties) {
        this.localStorageLocation = storageProperties.getLocalStorageLocation();
    }

    public RecoveredFile getImage(String imageName) {
       try {
           Path targetLocation = this.localStorageLocation.resolve(imageName);

           if (!Files.exists(targetLocation)) {
               throw new StorageNotFoundException("The file is not found.");
           }

           byte[] imageBytes = Files.readAllBytes(targetLocation);

           ByteArrayResource resource = new ByteArrayResource(imageBytes);

           return RecoveredFile.builder().resource(resource).build();
       } catch (IOException e) {
           throw new FileStorageException("the file cannot be recovered.", e);
       }
    }

    public void saveFile(MultipartFile file, String filename) {
        try {
            Path targetLocation = this.localStorageLocation.resolve(filename);

            file.transferTo(targetLocation);
        } catch(IOException e) {
            throw new FileStorageException("Error storing file "
                    + file.getOriginalFilename()
                    + ", please try again"
            );
        }
    }

    public void deleteFile(String filename) {
        try {
            Path targetLocation = this.localStorageLocation.resolve(filename);

            Files.deleteIfExists(targetLocation);
        }catch (IOException e) {
            throw new StorageNotFoundException("Error when deleting file");
        }
    }
}
