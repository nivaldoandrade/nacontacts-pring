package com.nasa.nacontacts.domain.services;

import com.nasa.nacontacts.domain.config.StorageProperties;
import com.nasa.nacontacts.domain.exceptions.FileStorageException;
import com.nasa.nacontacts.domain.exceptions.StorageNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class LocalStorageServiceTest {
    @Mock
    StorageProperties storageProperties;

    LocalStorageService localStorageService;

    Path localStorageLocation;

    @BeforeEach
    void setUp() {
        localStorageLocation = mock(Path.class);
        when(storageProperties.getLocalStorageLocation()).thenReturn(localStorageLocation);

        localStorageService = new LocalStorageService(storageProperties);
    }

    @Test
    void shouldGetImage() throws IOException {
        String imageName = "test.jpg";

        MockedStatic<Files> mockedFiles = mockStatic(Files.class);

        byte[] mockedByte = imageName.getBytes();

        when(localStorageLocation.resolve(imageName)).thenReturn(mock(Path.class));
        when(Files.exists(any(Path.class))).thenReturn(true);
        when(Files.readAllBytes(any(Path.class))).thenReturn(mockedByte);

        StorageService.RecoveredFile recoveredFileReturn = localStorageService.getImage(imageName);

        assertNotNull(recoveredFileReturn);
        assertInstanceOf(StorageService.RecoveredFile.class, recoveredFileReturn);
        mockedFiles.verify(() -> Files.exists(any(Path.class)));
        mockedFiles.verify(() -> Files.readAllBytes(any(Path.class)));


        mockedFiles.close();
    }

    @Test
    void shouldThrowErrorWhenFileNotFound() {
        String imageName = "test.jpg";

        MockedStatic<Files> mockedFiles = mockStatic(Files.class);

        when(localStorageLocation.resolve(imageName)).thenReturn(mock(Path.class));
        when(Files.exists(any(Path.class))).thenReturn(false);

        StorageNotFoundException e = assertThrows(
                StorageNotFoundException.class,
                () -> localStorageService.getImage(imageName)
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

        when(localStorageLocation.resolve(imageName)).thenReturn(mock(Path.class));
        when(Files.exists(any(Path.class))).thenReturn(true);
        when(Files.readAllBytes(any(Path.class))).thenThrow(new IOException());

        FileStorageException e  = assertThrows(
                FileStorageException.class,
                () -> localStorageService.getImage(imageName)
        );

        assertEquals(e.getMessage(), "the file cannot be recovered.");

        mockedFiles.verify(() -> Files.exists(any(Path.class)));

        mockedFiles.close();
    }

    @Test
    void shouldSaveFile() throws IOException {
        String originalFilename = "test.jpg";
        MultipartFile mockedFile = mock(MultipartFile.class);

        when(localStorageLocation.resolve(any(String.class))).thenReturn(mock(Path.class));

        localStorageService.saveFile(mockedFile, originalFilename);

        verify(mockedFile).transferTo(any(Path.class));
    }

    @Test
    void shouldThrowIOExceptionErrorWhenSaveFile() throws IOException {
        String originalFilename = "test.jpg";
        MultipartFile mockedFile = mock(MultipartFile.class);

        when(localStorageLocation.resolve(any(String.class))).thenReturn(mock(Path.class));
        when(mockedFile.getOriginalFilename()).thenReturn(originalFilename);

        String messageError = "Error storing file " + originalFilename + ", please try again";

        doThrow(new IOException(messageError))
                .when(mockedFile).transferTo(any(Path.class));

        FileStorageException e = assertThrows(
                FileStorageException.class,
                () -> localStorageService.saveFile(mockedFile, originalFilename)
        );

        assertEquals(e.getMessage(), messageError);
        verifyNoMoreInteractions(mockedFile);
    }

//    @Test
//    void shouldGenerateFilename() {
//        String originalFilename = "test.jpg";
//
//        String hashFilename = localStorageService.generateFileName(originalFilename);
//
//        String UUIDRandom = hashFilename.split("_")[0];
//
//        assertTrue(hashFilename.startsWith(UUIDRandom));
//        assertTrue(hashFilename.endsWith(originalFilename));
//    }

    @Test
    void shouldDeleteFile() throws IOException {

        MockedStatic<Files> mockedFiles = mockStatic(Files.class);

        when(localStorageLocation.resolve(any(String.class))).thenReturn(mock(Path.class));

        localStorageService.deleteFile("filename.jpg");

        mockedFiles.verify(() -> Files.deleteIfExists(any(Path.class)));
        mockedFiles.close();
    }

    @Test
    void shouldThrowIOExceptionErrorWhenDeleteFile() throws IOException {
        MockedStatic<Files> mockedFiles = mockStatic(Files.class);

        String messageError = "Error when deleting file";

        when(localStorageLocation.resolve(any(String.class))).thenReturn(mock(Path.class));
        doThrow(new IOException(messageError)).when(Files.class);
        Files.deleteIfExists(any(Path.class));

        StorageNotFoundException e = assertThrows(
                StorageNotFoundException.class,
                () -> localStorageService.deleteFile("filename.jpg")
        );

        mockedFiles.verify(() -> Files.deleteIfExists(any(Path.class)));
        assertEquals(e.getMessage(), messageError);
        mockedFiles.close();
    }

}
