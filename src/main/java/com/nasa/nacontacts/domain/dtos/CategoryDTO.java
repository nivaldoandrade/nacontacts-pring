package com.nasa.nacontacts.domain.dtos;

import com.nasa.nacontacts.domain.Entities.Category;

import java.util.UUID;

public record CategoryDTO(UUID id, String name) {

    public static CategoryDTO from(Category category) {
        return new CategoryDTO(category.getId(), category.getName());
    }
}
