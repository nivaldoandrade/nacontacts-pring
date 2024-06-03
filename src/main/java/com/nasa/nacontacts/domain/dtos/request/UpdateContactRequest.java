package com.nasa.nacontacts.domain.dtos.request;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.constraints.FileType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record UpdateContactRequest(
        @NotEmpty(message = "Name is required")
        String name,

        @NotEmpty(message = "Email is required")
        String email,

        String phone,

        @NotNull(message = "CategoryId is required")
        UUID category_id,

        @FileType(allowedExtensions = {".jpg", ".jpeg", ".png"})
        MultipartFile photo
) {

        public static UpdateContactRequest fromContact(Contact contact, MultipartFile file) {
                return new UpdateContactRequest(
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhone(),
                        contact.getCategory().getId(),
                        file
                );
        }

        public static UpdateContactRequest fromContactWithCategoryId(Contact contact, UUID categoryId) {
                return new UpdateContactRequest(
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhone(),
                        categoryId,
                        null
                );
        }

        public static Contact to(UUID id, UpdateContactRequest updateContactRequest, String photoName, Category category) {
                return Contact.builder()
                        .id(id)
                        .name(updateContactRequest.name())
                        .email(updateContactRequest.email())
                        .phone(updateContactRequest.phone())
                        .photo(photoName)
                        .category(category)
                        .build();
        }
}
