package com.nasa.nacontacts.domain.dtos.request;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.Entities.Contact;
import com.nasa.nacontacts.domain.constraints.FileType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record CreateContactRequest(

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
        public static CreateContactRequest fromContact(Contact contact, MultipartFile file) {
                return new CreateContactRequest(
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhone(),
                        contact.getCategory().getId(),
                        file
                );
        }

        public static CreateContactRequest fromContactWithCategoryId(Contact contact, UUID categoryId) {
                return new CreateContactRequest(
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhone(),
                        categoryId,
                        null
                );
        }

        public static Contact to(CreateContactRequest createContactRequest, String photoName, Category category) {
                return Contact.builder()
                        .name(createContactRequest.name())
                        .email(createContactRequest.email())
                        .phone(createContactRequest.phone())
                        .photo(photoName)
                        .category(category)
                        .build();
        }
}
