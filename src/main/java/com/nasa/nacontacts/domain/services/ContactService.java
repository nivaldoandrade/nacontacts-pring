package com.nasa.nacontacts.domain.services;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.dtos.request.CreateContactRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateContactRequest;
import com.nasa.nacontacts.domain.exceptions.EmailAlreadyInUseException;
import com.nasa.nacontacts.domain.exceptions.EntityNotFoundException;
import com.nasa.nacontacts.domain.repositories.ContactRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static com.nasa.nacontacts.domain.utils.StringUtils.removeAccents;

@Service
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

    public Page<Contact> list(Pageable pageable, String search) {
        Page<Contact> contacts = contactRepository.findAll(
               removeAccents(search),
               pageable
        );

        return contacts;
    }

    public Contact findById(UUID id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException(id, Contact.class));

        return contact;
    }


    @Transactional
    public Contact create(CreateContactRequest contact) {
        Category categoryExists = categoryService.findById(contact.category_id());

        Optional<Contact> contactByEmailExists = contactRepository.findByEmail(contact.email());

        if(contactByEmailExists.isPresent()) {
            throw new EmailAlreadyInUseException();
        }

        String photoName = handlePhoto(contact.photo());

        Contact newContact = CreateContactRequest.to(contact, photoName, categoryExists);

        return contactRepository.save(newContact);
    }

    @Transactional
    public void update(UUID id, UpdateContactRequest request) {

        Contact existingContact = this.findById(id);

        validateEmailUniqueness(request.email(), existingContact.getId());

        Category category = resolveCategory(existingContact, request.category_id());

        String photoName = handlePhoto(request.photo(), existingContact.getPhoto());

        Contact newContact = UpdateContactRequest.to(id, request, photoName, category);

        contactRepository.save(newContact);
    }

    @Transactional
    public void delete(UUID id) {
        Contact contact = this.findById(id);

        if(contact.getPhoto() != null) {
            fileUploadService.deleteFile(contact.getPhoto());
        }

        contactRepository.delete(contact);
    }

    private void validateEmailUniqueness(String email, UUID existingContactId) {
        Optional<Contact> contactByEmailExists = contactRepository.findByEmail(email);

        contactByEmailExists.ifPresent((c) -> {
            if(!c.getId().equals(existingContactId)) {
                throw new EmailAlreadyInUseException();
            }
        });
    }

    private Category resolveCategory(Contact existingContact, UUID categoryId) {
        return existingContact.getCategory().getId().equals(categoryId)
                ? existingContact.getCategory()
                : categoryService.findById(categoryId);
    }

    private String handlePhoto(MultipartFile newFile, String existingPhotoName) {
        if(newFile != null){
            String newPhotoName = fileUploadService.generateFileName(newFile.getOriginalFilename());
            fileUploadService.saveFile(newFile, newPhotoName);

            if(existingPhotoName != null) {
                fileUploadService.deleteFile(existingPhotoName);
            }

            return newPhotoName;
        }

        return existingPhotoName;
    }

    private String handlePhoto(MultipartFile newFile) {
        return handlePhoto(newFile, null);
    }
    ;}
