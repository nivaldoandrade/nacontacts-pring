package com.nasa.nacontacts.domain.dtos.request;

import com.nasa.nacontacts.domain.Entities.Contact;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateContactRequest(
        @NotEmpty(message = "Name is required")
        String name,

        @NotEmpty(message = "Email is required")
        String email,

        String phone,

        @NotNull(message = "CategoryId is required")
        UUID category_id
) {

        public static UpdateContactRequest fromContact(Contact contact) {
                return new UpdateContactRequest(
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhone(),
                        contact.getCategory().getId()
                );
        }

        public static UpdateContactRequest fromContactWithCategoryId(Contact contact, UUID categoryId) {
                return new UpdateContactRequest(
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhone(),
                        categoryId
                );
        }
}
