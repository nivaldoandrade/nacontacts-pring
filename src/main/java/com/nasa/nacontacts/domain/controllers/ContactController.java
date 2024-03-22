package com.nasa.nacontacts.domain.controllers;

import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.dtos.ContactDTO;
import com.nasa.nacontacts.domain.dtos.ListContactDTO;
import com.nasa.nacontacts.domain.dtos.request.CreateContactRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateContactRequest;
import com.nasa.nacontacts.domain.interfaces.FileType;
import com.nasa.nacontacts.domain.services.ContactService;
import com.nasa.nacontacts.domain.services.FileUploadService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    private final FileUploadService fileUploadService;


    public ContactController(
            ContactService contactService,
            FileUploadService fileUploadService
    ) {
        this.contactService = contactService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/image/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {

        ByteArrayResource resource = fileUploadService.getImage(imageName);

        MediaType contentType = imageName.endsWith(".png") ? MediaType.IMAGE_PNG :  MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<ListContactDTO> list(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "orderBy", defaultValue = "asc") String orderBy
    ) {
        Sort.Direction direction = "desc".equalsIgnoreCase(orderBy)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "name"));

        Page<Contact> contacts = contactService.findAll(pageable);

        ListContactDTO contactsDTO = ListContactDTO.from(contacts);

        return ResponseEntity.ok().body(contactsDTO);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> show(@PathVariable UUID id) {
        Contact contact = contactService.findById(id);

        return ResponseEntity.ok().body(ContactDTO.from(contact));
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> create(@ModelAttribute @Validated CreateContactRequest request,
                                         @RequestPart(required = false)
                                         @Validated @FileType(allowedExtensions = {".jpg", ".jpeg", ".png"})
                                         MultipartFile photo
    ) {
        Contact newContact = contactService.create(request, photo);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(newContact.getId()).toUri();


        return ResponseEntity.created(uri).body(newContact);
    }

    @PutMapping("/{id}")
    public  ResponseEntity<Void> update(@PathVariable UUID id,
                                        @ModelAttribute @Validated UpdateContactRequest request,
                                        @RequestPart(required = false)
                                        @Validated @FileType(allowedExtensions = {".jpg", ".jpeg", ".png"})
                                        MultipartFile photo
    ) {
        contactService.update(id, request, photo);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contactService.delete(id);

        return ResponseEntity.noContent().build();
    }
};
