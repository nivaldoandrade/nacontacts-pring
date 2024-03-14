package com.nasa.nacontacts.domain.services;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.dtos.request.CreateContactRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateContactRequest;
import com.nasa.nacontacts.domain.exceptions.EmailAlreadyInUseException;
import com.nasa.nacontacts.domain.exceptions.EntityNotFoundException;
import com.nasa.nacontacts.domain.repositories.ContactRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final CategoryService categoryService;
    private final FileUploadService fileUploadService;

    public ContactService(ContactRepository contactRepository,
                          CategoryService categoryService,
                          FileUploadService fileUploadService
    ) {
        this.contactRepository = contactRepository;
        this.categoryService = categoryService;
        this.fileUploadService = fileUploadService;
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

    public Contact create(CreateContactRequest contact, MultipartFile photo) {
        Category categoryExists = categoryService.findById(contact.category_id());

        Optional<Contact> contactByEmailExists = contactRepository.findByEmail(contact.email());

        if(contactByEmailExists.isPresent()) {
            throw new EmailAlreadyInUseException();
        }

        String photoName = photo != null
                ? fileUploadService.generateFileName(photo.getOriginalFilename())
                : null;

        Contact newContact = new Contact(
                null,
                contact.name(),
                contact.email(),
                contact.phone(),
                photoName,
                categoryExists
        );

       newContact = contactRepository.save(newContact);

       if(photoName != null) {
           fileUploadService.saveFile(photo, photoName);
       }

       return newContact;
    }

    public void update(UUID id, UpdateContactRequest request, MultipartFile photo) {
        Contact contactExists = this.findById(id);

        Optional<Contact> contactByEmailExists = contactRepository.findByEmail(request.email());

        contactByEmailExists.ifPresent((c) -> {
            if(!c.getId().equals(contactExists.getId())) {
                throw new EmailAlreadyInUseException();
            }
        });

        Category category = categoryService.findById(request.category_id());

        String photoName = photo != null
                ? fileUploadService.generateFileName(photo.getOriginalFilename())
                : contactExists.getPhoto();

        Contact newContact = new Contact(id,
                request.name(),
                request.email(),
                request.phone(),
                photoName,
                category
        );

        contactRepository.save(newContact);


        if(photo != null && photoName != null) {
            fileUploadService.saveFile(photo, photoName);
        }
    }

    public void delete(UUID id) {
        Contact contact = this.findById(id);

        contactRepository.delete(contact);
    }
;}
