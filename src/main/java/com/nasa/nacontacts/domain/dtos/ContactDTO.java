package com.nasa.nacontacts.domain.dtos;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.Entities.Contact;

import java.util.UUID;

public record ContactDTO(
        UUID id,

        String name,

        String email,

        String phone,

        String photo,

        String photoUrl,

        Category category
) {

    public static ContactDTO from(Contact contact) {
        return new ContactDTO(contact.getId()
                ,contact.getName()
                ,contact.getEmail()
                ,contact.getPhone()
                ,contact.getPhoto()
                ,contact.getPhotoUrl()
                ,contact.getCategory()
        );
    }
}
