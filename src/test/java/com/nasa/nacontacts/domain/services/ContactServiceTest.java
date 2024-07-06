package com.nasa.nacontacts.domain.services;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.dtos.request.CreateContactRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateContactRequest;
import com.nasa.nacontacts.domain.exceptions.EmailAlreadyInUseException;
import com.nasa.nacontacts.domain.exceptions.EntityNotFoundException;
import com.nasa.nacontacts.domain.repositories.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
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

    private Contact contact1;

    private Contact contact2;

    @BeforeEach
    void setUpd() {
        contact1 = new Contact(
                null,
                "contact1",
                "contact1@email.com",
                "contact1.jpg",
                "123456789",
                null,
                null
        );

        contact2 = new Contact(
                null,
                "contact2",
                "contact2@email.com",
                "contact2.jpg",
                "987654321",
                null,
                null
        );
    }

    @Test
    void shouldShowListContacts() {
        Page<Contact> contacts = new PageImpl<>(List.of(contact1, contact2));

        when(contactRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(contacts);

        Page<Contact> contactsReturn = contactService.list(Pageable.unpaged(), "");

        assertEquals(contacts, contactsReturn);
        verify(contactRepository).findAll(any(String.class), any(Pageable.class));
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldShowAscendingListContacts() {
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
        UUID contactId = UUID.randomUUID();

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact1));

        Contact contactReturn = contactService.findById(contactId);

        assertEquals(contact1, contactReturn);
        verify(contactRepository).findById(contactId);
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
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        contact1.setCategory(category);

        when(categoryService.findById(categoryId)).thenReturn(category);
        when(contactRepository.findByEmail(contact1.getEmail())).thenReturn(Optional.empty());
        when(contactRepository.save(contact1)).thenReturn(contact1);

        CreateContactRequest createContactRequest = CreateContactRequest.fromContact(contact1, mock(MultipartFile.class));
        Contact contactReturn = contactService.create(createContactRequest);

        assertEquals(contact1, contactReturn);
        verify(categoryService).findById(categoryId);
        verify(contactRepository).findByEmail(contact1.getEmail());
        verify(contactRepository).save(contact1);
        verify(storageService).saveFile(any(MultipartFile.class), any(String.class));
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldCreateContactWithoutPhoto() {
        Category mockedCategory = mock(Category.class);
        mockedCategory.setId(UUID.randomUUID());

        contact1.setCategory(mockedCategory);
        CreateContactRequest createContactRequest = CreateContactRequest.fromContact(contact1, null);

        when(categoryService.findById(mockedCategory.getId())).thenReturn(mockedCategory);
        when(contactRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(contactRepository.save(contact1)).thenReturn(contact1);

        contactService.create(createContactRequest);

        verify(categoryService).findById(mockedCategory.getId());
        verify(contactRepository).findByEmail(any(String.class));
        verify(contactRepository).save(contact1);
        verifyNoInteractions(storageService);
        verifyNoMoreInteractions(contactRepository);

    }

    @Test
    void shouldThrowErrorWhenCreateContactWithDuplicateEmail() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        contact1.setCategory(category);

        when(categoryService.findById(categoryId)).thenReturn(category);
        when(contactRepository.findByEmail(contact1.getEmail())).thenReturn(Optional.of(contact1));

        CreateContactRequest createContactRequest = CreateContactRequest.fromContact(contact1, null);

        EmailAlreadyInUseException e = assertThrows(
                EmailAlreadyInUseException.class,
                () -> contactService.create(createContactRequest)
        );

        assertEquals(e.getMessage(), "Email is already in use");
        verify(categoryService).findById(categoryId);
        verify(contactRepository).findByEmail(contact1.getEmail());
        verifyNoMoreInteractions(contactRepository);
        verifyNoInteractions(storageService);
    }

    @Test
    void shouldUpdateContact() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        UUID contactId = UUID.randomUUID();

        contact1.setId(contactId);
        contact1.setCategory(category);
        UpdateContactRequest updateContactRequest = UpdateContactRequest.fromContact(contact1,  mock(MultipartFile.class));
        contact1.setId(contactId);

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact1));
        when(contactRepository.findByEmail(updateContactRequest.email())).thenReturn(Optional.of(contact1));
        when(contactRepository.save(contact1)).thenReturn(contact1);

        contactService.update(contactId ,updateContactRequest);

        verify(contactRepository).findById(contactId);
        verify(contactRepository).findByEmail(updateContactRequest.email());
        verify(storageService).saveFile(any(MultipartFile.class), any(String.class));
        verify(contactRepository).save(contact1);
        verifyNoInteractions(categoryService);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldUpdateContactWithoutPhoto() {
        Category category = new Category(UUID.randomUUID(), "Facebook");

        UUID id = UUID.randomUUID();
        contact1.setCategory(category);
        UpdateContactRequest updateContactRequest = UpdateContactRequest.fromContact(contact1, null);
        contact1.setId(id);

        when(contactRepository.findById(id)).thenReturn(Optional.of(contact1));
        when(contactRepository.findByEmail(updateContactRequest.email())).thenReturn(Optional.of(contact1));

        contactService.update(id, updateContactRequest);

        verify(contactRepository).save(contact1);
        verifyNoMoreInteractions(contactRepository);
        verifyNoInteractions(storageService);
        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldUpdateContactWithNewCategory() {
        UUID categoryIdRequest = UUID.randomUUID();
        Category categoryRequest = new Category(categoryIdRequest, null);
        Category existingContactWithCategory = new Category(UUID.randomUUID(), "Twitter");

        UUID contact1Id = UUID.randomUUID();
        contact1.setId(contact1Id);
        Contact contactRequest = contact1;
        contactRequest.setCategory(categoryRequest);

        UpdateContactRequest updateContactRequest = UpdateContactRequest.fromContact(contactRequest, null);

        Contact existingContact = contact1;
        existingContact.setCategory(existingContactWithCategory);

        when(contactRepository.findById(contact1Id)).thenReturn(Optional.of(existingContact));
        when(contactRepository.findByEmail(any(String.class))).thenReturn(Optional.of(contactRequest));
        when(categoryService.findById(any(UUID.class))).thenReturn(mock(Category.class));

        contactService.update(contact1Id, updateContactRequest);

        verify(contactRepository).save(existingContact);
        verify(categoryService).findById(any(UUID.class));
    }

    @Test
    void shouldThrowErrorWhenUpdateContactWithDuplicateEmail() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        UUID contact1Id = UUID.randomUUID();
        contact1.setCategory(category);
        contact2.setCategory(category);

        UpdateContactRequest updateContactRequest = UpdateContactRequest.fromContact(contact1, null);
        contact1.setId(contact1Id);
        contact2.setId(UUID.randomUUID());

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

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact1));

        contactService.delete(contactId);

        verify(contactRepository).findById(contactId);
        verify(contactRepository).delete(contact1);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldDeleteContactWithPhoto() {
        contact1.setPhoto("uuid-photo.png");

        when(contactRepository.findById(any(UUID.class))).thenReturn(Optional.of(contact1));

        contactService.delete(UUID.randomUUID());

        verify(contactRepository).delete(contact1);
        verify(storageService).deleteFile(contact1.getPhoto());

    }
}
