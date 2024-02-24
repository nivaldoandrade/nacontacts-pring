package com.nasa.nacontacts.domain.services;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.dtos.request.CreateContactRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateContactRequest;
import com.nasa.nacontacts.domain.exceptions.EmailAlreadyInUseException;
import com.nasa.nacontacts.domain.exceptions.EntityNotFoundException;
import com.nasa.nacontacts.domain.repositories.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final CategoryService categoryService;

    public ContactService(ContactRepository contactRepository, CategoryService categoryService) {
        this.contactRepository = contactRepository;
        this.categoryService = categoryService;
    }

    public List<Contact> findAll() {
        List<Contact> contacts = contactRepository.findAll();

        return contacts;
    }

    public Contact findById(UUID id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException(id, Contact.class));

        return contact;
    }

    public Contact create(CreateContactRequest contact) {
        Category categoryExists = categoryService.findById(contact.category_id());

        Optional<Contact> contactByEmailExists = contactRepository.findByEmail(contact.email());

        if(contactByEmailExists.isPresent()) {
            throw new EmailAlreadyInUseException();
        }

        Contact newContact = new Contact(
                null
                , contact.name()
                ,contact.email()
                ,contact.phone()
                ,categoryExists
        );

        newContact = contactRepository.save(newContact);

        return newContact;
    }

    public void update(UUID id, UpdateContactRequest request) {
        Contact contactExists = this.findById(id);

        Optional<Contact> contactByEmailExists = contactRepository.findByEmail(request.email());

        contactByEmailExists.ifPresent((c) -> {
            if(!c.getId().equals(contactExists.getId())) {
                throw new EmailAlreadyInUseException();
            }
        });

        Category category = categoryService.findById(request.category_id());

        Contact newContact = new Contact(id,
                request.name(),
                request.email(),
                request.phone(),
                category
        );

        contactRepository.save(newContact);
    }

    public void delete(UUID id) {
        Contact contact = this.findById(id);

        contactRepository.delete(contact);
    }
;}
