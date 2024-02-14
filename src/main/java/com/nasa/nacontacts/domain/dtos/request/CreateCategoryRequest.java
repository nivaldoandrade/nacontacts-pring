package com.nasa.nacontacts.domain.dtos.request;

import com.nasa.nacontacts.domain.Entities.Category;
import jakarta.validation.constraints.NotEmpty;

public record CreateCategoryRequest(
        @NotEmpty(message = "Name is required")
        String name
) {

        public static CreateCategoryRequest fromCategory(Category category) {
                return new CreateCategoryRequest(category.getName());
        }
}
