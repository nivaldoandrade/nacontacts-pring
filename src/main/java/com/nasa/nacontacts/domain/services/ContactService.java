package com.nasa.nacontacts.domain.services;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.dtos.request.CreateContactRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateContactRequest;
import com.nasa.nacontacts.domain.exceptions.EmailAlreadyInUseException;
import com.nasa.nacontacts.domain.exceptions.EntityNotFoundException;
import com.nasa.nacontacts.domain.repositories.ContactRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public Page<Contact> findAll(Pageable pageable) {
        Page<Contact> contacts = contactRepository.findAll(pageable);

        return contacts;
    }

    public Contact findById(UUID id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException(id, Contact.class));

        return contact;
    }


    public Contact create(CreateContactRequest request) {
        MultipartFile photo = request.photo();
        Category categoryExists = categoryService.findById(request.category_id());

        Optional<Contact> contactByEmailExists = contactRepository.findByEmail(request.email());

        if(contactByEmailExists.isPresent()) {
            throw new EmailAlreadyInUseException();
        }

        String photoName = photo != null
                ? fileUploadService.generateFileName(photo.getOriginalFilename())
                : null;

        Contact newContact = CreateContactRequest.to(request, photoName, categoryExists);

       newContact = contactRepository.save(newContact);

       if(photoName != null) {
           fileUploadService.saveFile(photo, photoName);
       }

       return newContact;
    }

    public void update(UUID id, UpdateContactRequest request) {
        MultipartFile photo = request.photo();
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
