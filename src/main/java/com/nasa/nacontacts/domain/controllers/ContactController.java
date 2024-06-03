package com.nasa.nacontacts.domain.controllers;

import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.dtos.ContactDTO;
import com.nasa.nacontacts.domain.dtos.ListContactDTO;
import com.nasa.nacontacts.domain.dtos.request.CreateContactRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateContactRequest;
import com.nasa.nacontacts.domain.services.ContactService;
import com.nasa.nacontacts.domain.services.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;


@Tag(name = "Contact", description = "Contact management API")
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

    @Operation(summary = "Retrieve an Image Contact", description = "Get an image contact by name in JPEG and PNG format")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {@Content(
                            schema = @Schema(type = "string", format = "binary"),
                            mediaType = MediaType.IMAGE_PNG_VALUE
                    ),
                    @Content(
                            schema = @Schema(type = "string", format = "binary"),
                            mediaType = MediaType.IMAGE_JPEG_VALUE
                    )}
            ),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content),
    })
    @GetMapping("/image/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {

        ByteArrayResource resource = fileUploadService.getImage(imageName);

        MediaType contentType = imageName.endsWith(".png") ? MediaType.IMAGE_PNG :  MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(resource);
    }

    @Operation(summary = "Retrieve all Contact", description = "Get a Contacts array")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = { @Content(
                            schema = @Schema( implementation = ListContactDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )}
            ),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content),
    })
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

    @Operation(
            summary = "Retrieve a Contact by id",
            description = "Get a Contact by id. The response is object of the ContactDTO schema type"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {@Content(
                            schema = @Schema(implementation =  ContactDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )}
            ),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content),
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> show(@PathVariable UUID id) {
        Contact contact = contactService.findById(id);

        return ResponseEntity.ok().body(ContactDTO.from(contact));
    }

    @Operation(
            summary = "Create a Contact",
            description = "Create a new Contact by passing CreateContactRequest schema type. " +
                    "If a photo is attached, it should be in one of the following formats: \".jpg\", \".jpeg\", \".png\" "
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {@Content(
                            schema = @Schema(implementation = ContactDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )}
            ),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content),
    })
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> create(@ModelAttribute @Validated CreateContactRequest request) {
        Contact newContact = contactService.create(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(newContact.getId()).toUri();


        return ResponseEntity.created(uri).body(newContact);
    }

    @Operation(summary = "Update a Contact by id",
            description = "Update a Contact by passing UpdateContactRequest schema type. " +
            "If a photo is attached, it should be in one of the following formats: \".jpg\", \".jpeg\", \".png\" "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content),
    })
    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public  ResponseEntity<Void> update(@PathVariable UUID id,
                                        @ModelAttribute @Validated UpdateContactRequest request

    ) {
        contactService.update(id, request);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a Contact by id", description = "Delete a Contact by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contactService.delete(id);

        return ResponseEntity.noContent().build();
    }
};
