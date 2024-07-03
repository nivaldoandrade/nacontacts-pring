package com.nasa.nacontacts.domain.services;

import com.amazonaws.services.s3.AmazonS3;
import com.nasa.nacontacts.domain.config.StorageProperties;
import com.nasa.nacontacts.domain.exceptions.FileStorageException;
import com.nasa.nacontacts.domain.exceptions.StorageNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class S3StorageService implements StorageService {

    private final AmazonS3 s3Client;

    private final String bucketName;

    private final String cdnUrl;

    private final Path tempStorageLocation;

    public S3StorageService(AmazonS3 s3Client, StorageProperties storageProperties) {
        this.s3Client = s3Client;
        this.bucketName = storageProperties.getS3().getBucketName();
        this.cdnUrl = storageProperties.getCdnUrl();
        this.tempStorageLocation = storageProperties.getTempStorageLocation();
    }

    public RecoveredFile getImage(String fileName) {

        String url = cdnUrl + fileName;


        return RecoveredFile.builder().url(url).build();
    }

    public void saveFile(MultipartFile multipartFile, String fileName) {
        File file = this.tempStorageLocation.resolve(fileName).toFile();

        try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(multipartFile.getBytes());
        } catch (IOException e) {
            throw new FileStorageException("Error storing file "
                    + multipartFile.getOriginalFilename()
                    + ", please try again"
            );
        }

        s3Client.putObject(bucketName, fileName, file);

        file.delete();
    }

    public void deleteFile(String fileName) {
        try {
            s3Client.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            throw new StorageNotFoundException("Error when deleting file");
        }
    }
}
