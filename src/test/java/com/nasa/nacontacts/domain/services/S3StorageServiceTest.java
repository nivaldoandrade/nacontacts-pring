package com.nasa.nacontacts.domain.services;


import com.amazonaws.services.s3.AmazonS3;
import com.nasa.nacontacts.domain.config.StorageProperties;
import com.nasa.nacontacts.domain.exceptions.FileStorageException;
import com.nasa.nacontacts.domain.exceptions.StorageNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3StorageServiceTest {

    @Mock
    AmazonS3 s3Client;

    @Mock
    StorageProperties storageProperties;

    @Mock
    StorageProperties.S3 s3;

    S3StorageService s3StorageService;

    String bucketName;

    String cdnUrl;

    Path tempStorageLocation;

    @BeforeEach
    void setUp() {
        bucketName = "bucket-test";
        cdnUrl = "http://cdn.test.com/";
        tempStorageLocation = Path.of("temp");

        when(s3.getBucketName()).thenReturn(bucketName);
        when(storageProperties.getS3()).thenReturn(s3);
        when(storageProperties.getCdnUrl()).thenReturn(cdnUrl);
        when(storageProperties.getTempStorageLocation()).thenReturn(tempStorageLocation);

        s3StorageService = new S3StorageService(s3Client, storageProperties);
    }


    @Test
    void shouldGetImage() {
        String imageName = "test.png";

        StorageService.RecoveredFile recoveredFileReturn = s3StorageService.getImage(imageName);

        assertNotNull(recoveredFileReturn);
        assertInstanceOf(StorageService.RecoveredFile.class, recoveredFileReturn);
        assertEquals(recoveredFileReturn.getUrl(), cdnUrl + imageName);
    }

    @Test
    void shouldSaveFile() throws IOException {
        MultipartFile mockedMultipartFile = mock(MultipartFile.class);
        String imageName = "test.png";

        when(mockedMultipartFile.getBytes()).thenReturn(imageName.getBytes());

        s3StorageService.saveFile(mockedMultipartFile, imageName);

        verify(mockedMultipartFile, times(1)).getBytes();
        verify(s3Client, times(1)).putObject(eq(bucketName), eq(imageName), any(File.class));
    }

    @Test
    void shouldThrowIOExceptionErrorWhenSaveFile() throws IOException {
        MultipartFile mockedMultipartFile = mock(MultipartFile.class);
        String imageName = "test.png";

        String messageError = "Error storing file " + imageName + ", please try again";

        when(mockedMultipartFile.getOriginalFilename()).thenReturn(imageName);
        doThrow(new IOException())
                .when(mockedMultipartFile).getBytes();

        FileStorageException e = assertThrows(
                FileStorageException.class,
                () -> s3StorageService.saveFile(mockedMultipartFile, imageName)
        );


        assertEquals(e.getMessage(), messageError);
        verifyNoMoreInteractions(mockedMultipartFile);
    }

    @Test
    void shouldDeleteFile() {
        String imageName = "test.png";

        s3StorageService.deleteFile(imageName);

        verify(s3Client).deleteObject(bucketName, imageName);
        verifyNoMoreInteractions(s3Client);
    }

    @Test
    void shouldThrowExceptionErrorWhenDeleteFile() {
        doThrow(new RuntimeException())
                .when(s3Client).deleteObject(eq(bucketName), any(String.class));

        String messageError = "Error when deleting file";

        StorageNotFoundException e = assertThrows(
                StorageNotFoundException.class,
                () -> s3StorageService.deleteFile("")
        );

        assertEquals(e.getMessage(), messageError);
        verifyNoMoreInteractions(s3Client);
    }
}
