package com.nasa.nacontacts.domain.dtos.request;

import com.nasa.nacontacts.domain.Entities.Contact;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateContactRequest(

        @NotEmpty(message = "Name is required")
        String name,

        @NotEmpty(message = "Email is required")
        String email,

        String phone,

        @NotNull(message = "CategoryId is required")
        UUID category_id
) {
        public static CreateContactRequest fromContact(Contact contact) {
                return new CreateContactRequest(
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhone(),
                        contact.getCategory().getId()
                );
        }

        public static CreateContactRequest fromContactWithCategoryId(Contact contact, UUID categoryId) {
                return new CreateContactRequest(
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhone(),
                        categoryId
                );
        }
}
