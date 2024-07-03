package com.nasa.nacontacts.domain.services;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.dtos.request.CreateContactRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateContactRequest;
import com.nasa.nacontacts.domain.exceptions.EmailAlreadyInUseException;
import com.nasa.nacontacts.domain.exceptions.EntityNotFoundException;
import com.nasa.nacontacts.domain.repositories.ContactRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContactServiceTest {

    @InjectMocks
    ContactService contactService;

    @Mock
    ContactRepository contactRepository;

    @Mock
    CategoryService categoryService;

    @Mock
    StorageService storageService;

    @Test
    void shouldShowListContacts() {
        Contact contact1 = new Contact(null, "contact1", "contact1@email.com", "contact1.jpg", "123456789", null);
        Contact contact2 = new Contact(null, "contact2", "contact2@email.com","contact2.jpg", "987654321", null);

        Page<Contact> contacts = new PageImpl<>(List.of(contact1, contact2));

        when(contactRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(contacts);

        Page<Contact> contactsReturn = contactService.list(Pageable.unpaged(), "");

        assertEquals(contacts, contactsReturn);
        verify(contactRepository).findAll(any(String.class), any(Pageable.class));
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldShowAscendingListContacts() {
        Contact contact1 = new Contact(null, "contact1", "contact1@email.com", "contact1.jpg", "123456789", null);
        Contact contact2 = new Contact(null, "contact2", "contact2@email.com","contact2.jpg", "987654321", null);

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Contact> contacts = new PageImpl<>(List.of(contact1, contact2));

        when(contactRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(contacts);

        List<Contact> expectedContacts = List.of(contact1, contact2);

        Page<Contact> contactsReturn = contactService.list(pageable, "");

        assertEquals(expectedContacts, contactsReturn.toList());
        assertEquals(contacts.getSort(), contactsReturn.getSort());
        assertEquals(contacts.getSize(), contactsReturn.getSize());
        assertEquals(contacts.getTotalPages(), contactsReturn.getTotalPages());

        verify(contactRepository).findAll(any(String.class), eq(pageable));
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldShowDescendingListContacts() {
        Contact contact1 = new Contact(null, "contact1", "contact1@email.com", "contact1.jpg", "123456789", null);
        Contact contact2 = new Contact(null, "contact2", "contact2@email.com","contact2.jpg", "987654321", null);

        Sort sort = Sort.by(Sort.Direction.DESC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Contact> contacts = new PageImpl<>(List.of(contact2, contact1));

        when(contactRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(contacts);

        List<Contact> expectedContacts = List.of(contact2, contact1);

        Page<Contact> contactsReturn = contactService.list(pageable, "");

        assertEquals(expectedContacts, contactsReturn.toList());
        assertEquals(contacts.getSort(), contactsReturn.getSort());
        assertEquals(contacts.getSize(), contactsReturn.getSize());
        assertEquals(contacts.getTotalPages(), contactsReturn.getTotalPages());

        verify(contactRepository).findAll(any(String.class), eq(pageable));
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldShowFilteredListContacts() {
        Contact contact1 = new Contact(UUID.randomUUID(), "contact1", "contact1@email.com", "contact1.jpg", "123456789", null);

        Page<Contact> mockedContacts = new PageImpl<>(List.of(contact1));

        when(contactRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(mockedContacts);

        List<Contact> expectedContacts = List.of(contact1);

        Page<Contact> contactsReturn = contactService.list(Pageable.unpaged(), "contact1");

        assertEquals(expectedContacts, contactsReturn.toList());

        verify(contactRepository).findAll(any(String.class), any(Pageable.class));
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldFindByIdContact() {
        UUID id = UUID.randomUUID();
        Contact contact = new Contact(id, "contact1", "contact1@email.com", "contact1.jpg", "123456789", null);

        when(contactRepository.findById(id)).thenReturn(Optional.of(contact));

        Contact contactReturn = contactService.findById(id);

        assertEquals(contact, contactReturn);
        verify(contactRepository).findById(id);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldThrowErrorWhenContactIdNotFound() {
        UUID id = UUID.randomUUID();

        when(contactRepository.findById(id)).thenThrow(new EntityNotFoundException(id, Contact.class));

        EntityNotFoundException e = assertThrows(
                EntityNotFoundException.class,
                () -> contactService.findById(id)
        );

        assertThat(e.getMessage(), is(Contact.class.getSimpleName() + " with id = " + id + " not found"));
        verify(contactRepository).findById(id);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldCreateNewContact() {
        String photoName = "contact1.jpg";

        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        Contact contact = new Contact(null, "contact1", "contact1@email.com", "123456789", photoName, category);

        when(categoryService.findById(categoryId)).thenReturn(category);
        when(contactRepository.findByEmail(contact.getEmail())).thenReturn(Optional.empty());
        when(contactRepository.save(contact)).thenReturn(contact);

        CreateContactRequest createContactRequest = CreateContactRequest.fromContact(contact, mock(MultipartFile.class));
        Contact contactReturn = contactService.create(createContactRequest);

        assertEquals(contact, contactReturn);
        verify(categoryService).findById(categoryId);
        verify(contactRepository).findByEmail(contact.getEmail());
        verify(contactRepository).save(contact);
        verify(storageService).saveFile(any(MultipartFile.class), any(String.class));
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldCreateContactWithoutPhoto() {
        Category mockedCategory = mock(Category.class);
        mockedCategory.setId(UUID.randomUUID());

        Contact contact = new Contact(null, "contact1", "contact1@email.com", "123456789", null, mockedCategory);
        CreateContactRequest createContactRequest = CreateContactRequest.fromContact(contact, null);

        when(categoryService.findById(mockedCategory.getId())).thenReturn(mockedCategory);
        when(contactRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(contactRepository.save(contact)).thenReturn(contact);

        contactService.create(createContactRequest);

        verify(categoryService).findById(mockedCategory.getId());
        verify(contactRepository).findByEmail(any(String.class));
        verify(contactRepository).save(contact);
        verifyNoInteractions(storageService);
        verifyNoMoreInteractions(contactRepository);

    }

    @Test
    void shouldThrowErrorWhenCreateContactWithDuplicateEmail() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        Contact contact = new Contact(null, "contact1", "contact1@email.com","123456789", null, category);


        when(categoryService.findById(categoryId)).thenReturn(category);
        when(contactRepository.findByEmail(contact.getEmail())).thenReturn(Optional.of(contact));

        CreateContactRequest createContactRequest = CreateContactRequest.fromContact(contact, null);

        EmailAlreadyInUseException e = assertThrows(
                EmailAlreadyInUseException.class,
                () -> contactService.create(createContactRequest)
        );

        assertEquals(e.getMessage(), "Email is already in use");
        verify(categoryService).findById(categoryId);
        verify(contactRepository).findByEmail(contact.getEmail());
        verifyNoMoreInteractions(contactRepository);
        verifyNoInteractions(storageService);
    }

    @Test
    void shouldUpdateContact() {
//        MultipartFile mockedFile = mock(MultipartFile.class);
//        String originalFilename = "contact1.jpg";
        String photoName = "uuid-contact1.jpg";

        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        UUID contactId = UUID.randomUUID();
        Contact contact = new Contact(null, "contact1", "contact1@email.com","123456789",photoName, category);
        UpdateContactRequest updateContactRequest = UpdateContactRequest.fromContact(contact,  mock(MultipartFile.class));
        contact.setId(contactId);

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));
        when(contactRepository.findByEmail(updateContactRequest.email())).thenReturn(Optional.of(contact));
//        when(mockedFile.getOriginalFilename()).thenReturn(originalFilename);
//        when(localStorageService.generateFileName(originalFilename)).thenReturn(photoName);
        when(contactRepository.save(contact)).thenReturn(contact);

        contactService.update(contactId ,updateContactRequest);

        verify(contactRepository).findById(contactId);
        verify(contactRepository).findByEmail(updateContactRequest.email());
//        verify(localStorageService).generateFileName(originalFilename);
//        verify(localStorageService).saveFile(mockedFile, photoName);
        verify(storageService).saveFile(any(MultipartFile.class), any(String.class));
        verify(contactRepository).save(contact);
        verifyNoInteractions(categoryService);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldUpdateContactWithoutPhoto() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        UUID id = UUID.randomUUID();
        Contact contact = new Contact(null, "contact1", "contact1@email.com","123456789",null, category);
        UpdateContactRequest updateContactRequest = UpdateContactRequest.fromContact(contact, null);
        contact.setId(id);

        when(contactRepository.findById(id)).thenReturn(Optional.of(contact));
        when(contactRepository.findByEmail(updateContactRequest.email())).thenReturn(Optional.of(contact));

        contactService.update(id, updateContactRequest);

        verify(contactRepository).save(contact);
        verifyNoMoreInteractions(contactRepository);
        verifyNoInteractions(storageService);
        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldUpdateContactWithNewCategory() {
        Category categoryRequest = new Category(UUID.randomUUID(), "Facebook");
        Category categoryReturn = new Category(UUID.randomUUID(), "Facebook");

        UUID id = UUID.randomUUID();
        Contact contactRequest = new Contact(null, "contact1", "contact1@email.com","123456789",null, categoryRequest);
        Contact contactReturn = new Contact(id, "contact1", "contact1@email.com","123456789",null, categoryReturn);
        UpdateContactRequest updateContactRequest = UpdateContactRequest.fromContact(contactRequest, null);


        when(contactRepository.findById(id)).thenReturn(Optional.of(contactReturn));
        when(contactRepository.findByEmail(any(String.class))).thenReturn(Optional.of(contactReturn));
        when(categoryService.findById(any(UUID.class))).thenReturn(mock(Category.class));

        contactService.update(id, updateContactRequest);

        verify(contactRepository).save(contactReturn);
        verify(categoryService).findById(any(UUID.class));
    }

    @Test
    void shouldThrowErrorWhenUpdateContactWithDuplicateEmail() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        UUID contact1Id = UUID.randomUUID();
        Contact contact1 = new Contact(null, "contact1", "contact1@email.com", "123456789",null,category);

        UpdateContactRequest updateContactRequest = UpdateContactRequest.fromContact(contact1, null);
        contact1.setId(contact1Id);
        Contact contact2 = new Contact(UUID.randomUUID(), "contact2", "contact2@email.com", "123456789",null,category);

        when(contactRepository.findById(any(UUID.class))).thenReturn(Optional.of(contact1));
        when(contactRepository.findByEmail(any(String.class))).thenReturn(Optional.of(contact2));

        EmailAlreadyInUseException e = assertThrows(
                EmailAlreadyInUseException.class,
                () -> contactService.update(contact1Id ,updateContactRequest)
        );

        assertEquals(e.getMessage(), "Email is already in use");
        verify(contactRepository).findById(contact1Id);
        verify(contactRepository).findByEmail(updateContactRequest.email());
        verifyNoMoreInteractions(contactRepository);
        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldDeleteContact() {
        UUID contactId = UUID.randomUUID();
        Contact contact = new Contact(contactId, "contact", "contact@email.com", "123456789", null ,null);

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));

        contactService.delete(contactId);

        verify(contactRepository).findById(contactId);
        verify(contactRepository).delete(contact);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldDeleteContactWithPhoto() {
        Contact contact = new Contact();
        contact.setPhoto("uuid-photo.png");

        when(contactRepository.findById(any(UUID.class))).thenReturn(Optional.of(contact));

        contactService.delete(UUID.randomUUID());

        verify(contactRepository).delete(contact);
        verify(storageService).deleteFile(contact.getPhoto());

    }
}
