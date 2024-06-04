package com.nasa.nacontacts.domain.services;

import com.nasa.nacontacts.domain.config.FileStorageConfig;
import com.nasa.nacontacts.domain.exceptions.FileStorageException;
import com.nasa.nacontacts.domain.exceptions.StorageNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileUploadServiceTest {
    @Mock
    FileStorageConfig fileStorageConfig;

    FileUploadService fileUploadService;

    @BeforeEach
    void setUp() {
        String uploadDir = "uploads";
        when(fileStorageConfig.getUploadDir()).thenReturn(uploadDir);

        fileUploadService = new FileUploadService(fileStorageConfig);
    }

    @Test
    void shouldGetImage() throws IOException {
        String imageName = "test.jpg";

        byte[] mockedByte =  imageName.getBytes();
        MockedStatic<Files> mockedFiles = mockStatic(Files.class);

        when(Files.exists(any(Path.class))).thenReturn(true);
        when(Files.readAllBytes(any(Path.class))).thenReturn(mockedByte);

        fileUploadService.getImage(imageName);

       mockedFiles.verify(() -> Files.exists(any(Path.class)));
       mockedFiles.verify(() -> Files.readAllBytes(any(Path.class)));


        mockedFiles.close();
    }

    @Test
    void shouldThrowErrorWhenFileNotFound() {
        String imageName = "test.jpg";

        MockedStatic<Files> mockedFiles = mockStatic(Files.class);

        when(Files.exists(any(Path.class))).thenReturn(false);

        StorageNotFoundException e = assertThrows(
                StorageNotFoundException.class,
                () -> fileUploadService.getImage(imageName)
        );

        assertEquals(e.getMessage(), "The file is not found.");

        mockedFiles.verify(() -> Files.exists(any(Path.class)));
        mockedFiles.verifyNoMoreInteractions();

        mockedFiles.close();
    }

    @Test
    void shouldThrowIOExceptionErrorWhenGetImage() throws IOException {
        String imageName = "test.jpg";
        MockedStatic<Files> mockedFiles = mockStatic(Files.class);

        when(Files.exists(any(Path.class))).thenReturn(true);
        when(Files.readAllBytes(any(Path.class))).thenThrow(new IOException());

        FileStorageException e  = assertThrows(
                FileStorageException.class,
                () -> fileUploadService.getImage(imageName)
        );

        assertEquals(e.getMessage(), "the file cannot be recovered.");

        mockedFiles.verify(() -> Files.exists(any(Path.class)));

        mockedFiles.close();
    }

    @Test
    void shouldSaveFile() throws IOException {
        String originalFilename = "test.jpg";
        MultipartFile mockedFile = mock(MultipartFile.class);

        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);

        fileUploadService.saveFile(mockedFile, originalFilename);

        verify(mockedFile).transferTo(pathCaptor.capture());
    }

    @Test
    void shouldThrowIOExceptionErrorWhenSaveFile() throws IOException {
        String originalFilename = "test.jpg";
        MultipartFile mockedFile = mock(MultipartFile.class);

        when(mockedFile.getOriginalFilename()).thenReturn(originalFilename);

        String messageError = "Error storing file " + originalFilename + ", please try again";

        doThrow(new IOException(messageError))
                .when(mockedFile).transferTo(any(Path.class));

        FileStorageException e = assertThrows(
                FileStorageException.class,
                () -> fileUploadService.saveFile(mockedFile, originalFilename)
        );

        assertEquals(e.getMessage(), messageError);
        verifyNoMoreInteractions(mockedFile);
    }

    @Test
    void shouldGenerateFilename() {
        String originalFilename = "test.jpg";

        String hashFilename = fileUploadService.generateFileName(originalFilename);

        String UUIDRandom = hashFilename.split("_")[0];

        assertTrue(hashFilename.startsWith(UUIDRandom));
        assertTrue(hashFilename.endsWith(originalFilename));
    }

    @Test
    void shouldInitSuccess() throws IOException {
        MockedStatic<Files> mockedFiles = mockStatic(Files.class);
        Path mockPath = mock(Path.class);
        when(Files.createDirectories(any(Path.class))).thenReturn(mockPath);

        fileUploadService.init();

        mockedFiles.verify(() -> Files.createDirectories(any(Path.class)));
        mockedFiles.close();
    }

    @Test
    void shouldInitFailure() throws IOException {
        MockedStatic<Files> mockedFiles = mockStatic(Files.class);

        String messageError = "Error creating folder where files will be stored";

        doThrow(new FileStorageException(messageError)).when(Files.class);
        Files.createDirectories(any(Path.class));

        FileStorageException e = assertThrows(
                FileStorageException.class,
                () -> fileUploadService.init()
        );

        assertEquals(e.getMessage(), messageError);
        mockedFiles.close();
    }

    @Test
    void shouldDeleteFile() throws IOException {

        MockedStatic<Files> mockedFiles = mockStatic(Files.class);

        fileUploadService.deleteFile("filename.jpg");

        mockedFiles.verify(() -> Files.deleteIfExists(any(Path.class)));
        mockedFiles.close();
    }

    @Test
    void shouldThrowIOExceptionErrorWhenDeleteFile() throws IOException {
        MockedStatic<Files> mockedFiles = mockStatic(Files.class);

        String messageError = "Error when deleting file";
        doThrow(new IOException(messageError)).when(Files.class);
        Files.deleteIfExists(any(Path.class));

        StorageNotFoundException e = assertThrows(
                StorageNotFoundException.class,
                () -> fileUploadService.deleteFile("filename.jpg")
        );

        mockedFiles.verify(() -> Files.deleteIfExists(any(Path.class)));
        assertEquals(e.getMessage(), messageError);
        mockedFiles.close();
    }

}
