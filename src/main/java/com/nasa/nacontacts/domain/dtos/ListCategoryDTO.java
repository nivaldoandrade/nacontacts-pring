package com.nasa.nacontacts.domain.dtos;

import com.nasa.nacontacts.domain.Entities.Category;
import org.springframework.data.domain.Page;

import java.util.List;

public record ListCategoryDTO(
        List<CategoryDTO> category,
        Long totalItems,
        Integer totalPages
) {

    public static ListCategoryDTO from(Page<Category> pageCategory) {
        List<CategoryDTO> categories = pageCategory.stream().map(CategoryDTO::from).toList();

        return new ListCategoryDTO(
                categories,
                pageCategory.getTotalElements(),
                pageCategory.getTotalPages()
        );
    }
}
