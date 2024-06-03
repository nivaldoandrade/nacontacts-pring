package com.nasa.nacontacts.domain.controllers;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.dtos.ContactDTO;
import com.nasa.nacontacts.domain.dtos.ListContactDTO;
import com.nasa.nacontacts.domain.dtos.request.CreateContactRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateContactRequest;
import com.nasa.nacontacts.domain.exceptions.EmailAlreadyInUseException;
import com.nasa.nacontacts.domain.exceptions.EntityNotFoundException;
import com.nasa.nacontacts.domain.exceptions.GlobalExceptionHandler;
import com.nasa.nacontacts.domain.exceptions.StorageNotFoundException;
import com.nasa.nacontacts.domain.services.ContactService;
import com.nasa.nacontacts.domain.services.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ContactControllerTest {

    @InjectMocks
    ContactController contactController;

    @Mock
    ContactService contactService;

    @Mock
    FileUploadService fileUploadService;

    private MockMvc mockMvc;

    private String json;

    private String url;

    private Contact contact;

    private UUID id;

    private static final String emailAlreadyInUseMessage = "Email is already in use";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final MockMultipartFile mockedFile = new MockMultipartFile("photo", "contact1.jpg", MediaType.IMAGE_JPEG_VALUE, "test data".getBytes());;

    @BeforeEach
    void setUp() throws JsonMappingException {
        mockMvc = MockMvcBuilders.standaloneSetup(contactController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .alwaysDo(print()).build();
        url = "/contacts";

        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

       id = UUID.randomUUID();
       contact = new Contact(
               id,
               "contact",
               "contact@email.com",
               "123456789",
               null,
               category
       );

    }

    @Test
    void shouldGetJPEGImageContact() throws Exception{
        String imageName = "test.jpg";
        byte[] mockedBytes = imageName.getBytes();
        ByteArrayResource mockedByteArrayResource = new ByteArrayResource(mockedBytes);

        when(fileUploadService.getImage(imageName)).thenReturn(mockedByteArrayResource);

        mockMvc.perform(get(url + "/image/" + imageName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(content().bytes(mockedBytes));
    }
    @Test
    void shouldGetPNGImageContact() throws Exception{
        String imageName = "test.png";
        byte[] mockedBytes = imageName.getBytes();
        ByteArrayResource mockedByteArrayResource = new ByteArrayResource(mockedBytes);

        when(fileUploadService.getImage(imageName)).thenReturn(mockedByteArrayResource);

        mockMvc.perform(get(url + "/image/" + imageName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
                .andExpect(content().bytes(mockedBytes));
    }

    @Test
    void shouldThrowNotFoundErrorWhenGetImage() throws Exception {
        String imageName = "test.png";

        String notFoundMessage = "The file is not found.";

        when(fileUploadService.getImage(imageName)).thenThrow(new StorageNotFoundException(notFoundMessage));

        mockMvc.perform(get(url + "/image/" + imageName))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(notFoundMessage));
    }

    @Test
    void shouldShowListContacts() throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Contact> mockedContacts = new PageImpl<>(List.of(contact), pageable, 1);

        when(contactService.findAll(pageable)).thenReturn(mockedContacts);

        String expectedContacts = objectMapper.writeValueAsString(ListContactDTO.from(mockedContacts));

       mockMvc.perform(get(url)
               .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().json(expectedContacts));

        verify(contactService).findAll(pageable);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldShowAscendingListContacts() throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Contact> mockedContacts = new PageImpl<>(List.of(contact), pageable, 1);

        when(contactService.findAll(pageable)).thenReturn(mockedContacts);

        String expectedContacts = objectMapper.writeValueAsString(ListContactDTO.from(mockedContacts));

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .param("orderBy", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedContacts));


        verify(contactService).findAll(pageable);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldShowDescendingListContacts() throws Exception {
        Sort sort = Sort.by(Sort.Direction.DESC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Contact> mockedContacts = new PageImpl<>(List.of(contact), pageable, 1);

        System.out.println(pageable.getSort().descending());
        when(contactService.findAll(pageable)).thenReturn(mockedContacts);

        String expectedContacts = objectMapper.writeValueAsString(ListContactDTO.from(mockedContacts));

        mockMvc.perform(get(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("orderBy", "desc"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedContacts));


        verify(contactService).findAll(pageable);
        verifyNoMoreInteractions(contactService);
    }
    @Test
    void shouldReturnEmptyList() throws Exception {
        Page<Contact> mockedContacts = new PageImpl<>(Collections.emptyList());
        when(contactService.findAll(any(Pageable.class))).thenReturn(mockedContacts);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));

        verify(contactService).findAll(any(Pageable.class));
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldShowContactById() throws Exception {
        when(contactService.findById(id)).thenReturn(contact);

        String expectedJson = objectMapper.writeValueAsString(contact);

        mockMvc.perform(get(url + "/" + id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(contactService).findById(id);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldGet404WhenContactNoExists() throws Exception {
        when(contactService.findById(id)).thenThrow(new EntityNotFoundException(id, Contact.class));

        String message = Contact.class.getSimpleName() + " with id = " + id + " not found";

        mockMvc.perform(get(url + "/" + id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message));

        verify(contactService).findById(id);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldCreateNewContact() throws Exception {
        CreateContactRequest contactRequest = CreateContactRequest.fromContact(contact, mockedFile);

        contact.setPhoto(UUID.randomUUID() + "_" + mockedFile.getOriginalFilename());
        when(contactService.create(contactRequest)).thenReturn(contact);

        String expectedJson = objectMapper.writeValueAsString(ContactDTO.from(contact));
        String expectedLocation = "http://localhost/contacts/" + id;

        mockMvc.perform(multipart(url)
                .file(mockedFile)
                .param("name", contactRequest.name())
                .param("email", contactRequest.email())
                .param("phone", contactRequest.phone())
                .param("category_id", String.valueOf(contactRequest.category_id())))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedJson))
                .andExpect(header().string("Location", expectedLocation));

        verify(contactService).create(contactRequest);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldThrowErrorWhenCreatingContactWithDuplicateEmail() throws  Exception {
        CreateContactRequest contactRequest = CreateContactRequest.fromContact(contact, mockedFile);

        when(contactService.create(contactRequest)).thenThrow(new EmailAlreadyInUseException());

        mockMvc.perform(multipart(url)
                .file(mockedFile)
                .param("name", contactRequest.name())
                .param("email", contactRequest.email())
                .param("phone", contactRequest.phone())
                .param("category_id", String.valueOf(contactRequest.category_id())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(emailAlreadyInUseMessage));

        verify(contactService).create(contactRequest);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldThrowErrorWhenCreatingContactAndCategoryNonExists() throws Exception {
        CreateContactRequest contactRequest = CreateContactRequest.fromContact(contact, mockedFile);

        when(contactService.create(contactRequest)).thenThrow(
                new EntityNotFoundException(contact.getCategory().getId(), Category.class)
        );

        mockMvc.perform(multipart(url)
                .file(mockedFile)
                .param("name", contactRequest.name())
                .param("email", contactRequest.email())
                .param("phone", contactRequest.phone())
                .param("category_id", String.valueOf(contactRequest.category_id())))
                .andExpect(status().isNotFound());

        verify(contactService).create(contactRequest);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldThrowErrorWhenCreatingContactWithInvalidData() throws Exception {
        mockMvc.perform(multipart(url)
                .file(mockedFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field")
                        .value(hasItems("name", "email", "category_id")))
                .andExpect(jsonPath("$.fieldErrors[*].message")
                        .value(hasItems("Name is required", "Email is required", "CategoryId is required")));

        verifyNoInteractions(contactService);
    }

    @Test
    void shouldUpdatedContact() throws Exception {
        UpdateContactRequest contactRequest = UpdateContactRequest.fromContact(contact, mockedFile);

        mockMvc.perform(multipart(HttpMethod.PUT,url + "/" + id)
                .file(mockedFile)
                .param("name", contactRequest.name())
                .param("email", contactRequest.email())
                .param("phone", contactRequest.phone())
                .param("category_id", String.valueOf(contactRequest.category_id())))
                .andExpect(status().isNoContent());


        verify(contactService).update(id, contactRequest);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldThrowErrorWhenUpdatingContactNonExists() throws Exception {
        UpdateContactRequest contactRequest = UpdateContactRequest.fromContact(contact, mockedFile);

        doThrow(new EntityNotFoundException(id, Contact.class))
                .when(contactService).update(id, contactRequest);

        String message = Contact.class.getSimpleName() + " with id = " + id + " not found";

        mockMvc.perform(multipart(HttpMethod.PUT, url + "/" + id)
                .file(mockedFile)
                .param("name", contactRequest.name())
                .param("email", contactRequest.email())
                .param("phone", contactRequest.phone())
                .param("category_id", String.valueOf(contactRequest.category_id())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message));;

        verify(contactService).update(id, contactRequest);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldThrowErrorWhenUpdatingContactWithDuplicateEmail() throws Exception {
        UpdateContactRequest contactRequest = UpdateContactRequest.fromContact(contact, mockedFile);

        doThrow(new EmailAlreadyInUseException())
                .when(contactService).update(id, contactRequest);

        mockMvc.perform(multipart(HttpMethod.PUT, url + "/" + id)
                .file(mockedFile)
                .param("name", contactRequest.name())
                .param("email", contactRequest.email())
                .param("phone", contactRequest.phone())
                .param("category_id", String.valueOf(contactRequest.category_id())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(emailAlreadyInUseMessage));;

        verify(contactService).update(id, contactRequest);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldThrowErrorUpdatingContactWithCategoryNonExists() throws Exception{
        UUID categoryId = UUID.randomUUID();

        UpdateContactRequest contactRequest = UpdateContactRequest.fromContact(contact, mockedFile);

        doThrow(new EntityNotFoundException(categoryId, Category.class))
                .when(contactService).update(id, contactRequest);

        String message = Category.class.getSimpleName() + " with id = " + categoryId + " not found";

        mockMvc.perform(multipart(HttpMethod.PUT, url + "/" + id)
                .file(mockedFile)
                .param("name", contactRequest.name())
                .param("email", contactRequest.email())
                .param("phone", contactRequest.phone())
                .param("category_id", String.valueOf(contactRequest.category_id())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message));;

        verify(contactService).update(id, contactRequest);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldThrowErrorWhenUpdatingContactWithInvalidData() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, url + "/" + id)
                .file(mockedFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field")
                        .value(hasItems("name", "email", "category_id")))
                .andExpect(jsonPath("$.fieldErrors[*].message")
                        .value(hasItems("Name is required", "Email is required", "CategoryId is required")));;

        verifyNoInteractions(contactService);
    }

    @Test
    void shouldDeleteContact() throws Exception {

        mockMvc.perform(delete(url + "/" + id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());


        verify(contactService).delete(id);
        verifyNoMoreInteractions(contactService);
    }

    @Test
    void shouldThrowErrorWhenDeletingContactNonExists() throws Exception {

        doThrow(new EntityNotFoundException(id, Contact.class)).when(contactService).delete(id);

        String message = Contact.class.getSimpleName() + " with id = " + id + " not found";

        mockMvc.perform(delete(url + "/" + id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message));

        verify(contactService).delete(id);
        verifyNoMoreInteractions(contactService);
    }
}
