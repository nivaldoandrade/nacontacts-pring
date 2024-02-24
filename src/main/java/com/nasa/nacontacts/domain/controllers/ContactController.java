package com.nasa.nacontacts.domain.controllers;

import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.dtos.ContactDTO;
import com.nasa.nacontacts.domain.dtos.request.CreateContactRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateContactRequest;
import com.nasa.nacontacts.domain.services.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<List<ContactDTO>> list() {
        List<Contact> contacts = contactService.findAll();

        List<ContactDTO> contactsDTO = contacts.stream().map(ContactDTO::from).toList();

        return ResponseEntity.ok().body(contactsDTO);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> show(@PathVariable UUID id) {
        Contact contact = contactService.findById(id);

        return ResponseEntity.ok().body(ContactDTO.from(contact));
    }

    @PostMapping
    public ResponseEntity<ContactDTO> create(@RequestBody @Validated CreateContactRequest request) {
        Contact newContact = contactService.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(newContact.getId()).toUri();

        return ResponseEntity.created(uri).body(ContactDTO.from(newContact));
    }

    @PutMapping("/{id}")
    public  ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody @Validated UpdateContactRequest request) {
        contactService.update(id, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contactService.delete(id);

        return ResponseEntity.noContent().build();
    }
};
