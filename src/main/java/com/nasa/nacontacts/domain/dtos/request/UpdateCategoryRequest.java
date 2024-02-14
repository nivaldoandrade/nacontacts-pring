package com.nasa.nacontacts.domain.dtos.request;

import com.nasa.nacontacts.domain.Entities.Category;
import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

public record UpdateCategoryRequest(
        @NotEmpty(message = "Name is required")
        String name
) {

        public static UpdateCategoryRequest fromCategory(Category category) {
                return new UpdateCategoryRequest(category.getName());
        }
}
