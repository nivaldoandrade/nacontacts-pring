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

import java.util.Arrays;
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

    @Test
    void shouldShowListCategories() {
        Contact contact1 = new Contact(null, "contact1", "contact1@email.com", "123456789", null);
        Contact contact2 = new Contact(null, "contact2", "contact2@email.com", "987654321", null);

        List<Contact> contacts = Arrays.asList(contact1, contact2);

        when(contactRepository.findAll()).thenReturn(contacts);

        List<Contact> contactsReturn = contactService.findAll();

        assertEquals(contacts, contactsReturn);
        verify(contactRepository).findAll();
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldFindByIdContact() {
        UUID id = UUID.randomUUID();
        Contact contact = new Contact(id, "contact1", "contact1@email.com", "123456789", null);

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
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        Contact contact = new Contact(null, "contact1", "contact1@email.com", "123456789", category);

        when(categoryService.findById(categoryId)).thenReturn(category);
        when(contactRepository.findByEmail(contact.getEmail())).thenReturn(Optional.empty());
        when(contactRepository.save(contact)).thenReturn(contact);

        CreateContactRequest createContactRequest = CreateContactRequest.fromContact(contact);
        Contact contactReturn = contactService.create(createContactRequest);

        assertEquals(contact, contactReturn);
        verify(categoryService).findById(categoryId);
        verify(contactRepository).findByEmail(contact.getEmail());
        verify(contactRepository).save(contact);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldThrowErrorWhenCreateContactWithDuplicateEmail() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        Contact contact = new Contact(null, "contact1", "contact1@email.com", "123456789", category);

        when(categoryService.findById(categoryId)).thenReturn(category);
        when(contactRepository.findByEmail(contact.getEmail())).thenThrow(new EmailAlreadyInUseException());

        CreateContactRequest createContactRequest = CreateContactRequest.fromContact(contact);

        EmailAlreadyInUseException e = assertThrows(
                EmailAlreadyInUseException.class,
                () -> contactService.create(createContactRequest)
        );

        assertEquals(e.getMessage(), "Email is already in use");
        verify(categoryService).findById(categoryId);
        verify(contactRepository).findByEmail(contact.getEmail());
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldUpdateContact() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        UUID contactId = UUID.randomUUID();
        Contact contact = new Contact(null, "contact1", "contact1@email.com", "123456789", category);
        UpdateContactRequest updateContactRequest = UpdateContactRequest.fromContact(contact);

        contact.setId(contactId);

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));
        when(contactRepository.findByEmail(updateContactRequest.email())).thenReturn(Optional.of(contact));
        when(categoryService.findById(categoryId)).thenReturn(category);
        when(contactRepository.save(contact)).thenReturn(contact);

        contactService.update(contactId ,updateContactRequest);

        verify(contactRepository).findById(contactId);
        verify(contactRepository).findByEmail(updateContactRequest.email());
        verify(categoryService).findById(categoryId);
        verify(contactRepository).save(contact);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    void shouldThrowErrorWhenUpdateContactWithDuplicateEmail() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, "Facebook");

        UUID contact1Id = UUID.randomUUID();
        Contact contact1 = new Contact(null, "contact1", "contact1@email.com", "123456789", category);
        UpdateContactRequest updateContactRequest = UpdateContactRequest.fromContact(contact1);

        Contact contact2 = new Contact(UUID.randomUUID(), "contact2", "contact2@email.com", "123456789", null);

        when(contactRepository.findById(contact1Id)).thenReturn(Optional.of(contact1));
        when(contactRepository.findByEmail(updateContactRequest.email())).thenReturn(Optional.of(contact2));

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
        Contact contact = new Contact(contactId, "contact", "contact@email.com", "123456789", null);

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));

        contactService.delete(contactId);

        verify(contactRepository).findById(contactId);
        verify(contactRepository).delete(contact);
        verifyNoMoreInteractions(contactRepository);

    }
}
