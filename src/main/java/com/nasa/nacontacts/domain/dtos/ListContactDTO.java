package com.nasa.nacontacts.domain.dtos;

import com.nasa.nacontacts.domain.Entities.Contact;
import org.springframework.data.domain.Page;

import java.util.List;

public record ListContactDTO(
        List<ContactDTO> contacts,
        Long totalItems,
        Integer totalPages
) {

    public static ListContactDTO from(Page<Contact> pageCategory) {
        List<ContactDTO> contacts = pageCategory.stream().map(ContactDTO::from).toList();

        return new ListContactDTO(
                contacts,
                pageCategory.getTotalElements(),
                pageCategory.getTotalPages()
        );
    }
}
