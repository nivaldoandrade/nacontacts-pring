package com.nasa.nacontacts.domain.services;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.dtos.request.CreateCategoryRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateCategoryRequest;
import com.nasa.nacontacts.domain.exceptions.CategoryExistsException;
import com.nasa.nacontacts.domain.exceptions.EntityNotFoundException;
import com.nasa.nacontacts.domain.repositories.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.nasa.nacontacts.domain.utils.StringUtils.removeAccents;

@Service
@Transactional()
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Page<Category> list(Pageable pageable,String search) {

        String searchNormalize = removeAccents(search);

        Page<Category> categories = categoryRepository.findAll(
                searchNormalize,
                pageable
        );

        return categories;
    }

    public Category findById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id, Category.class));

        return category;
    }

    public Category create(CreateCategoryRequest category) {
        Optional<Category> categoryExists = categoryRepository.findByName(category.name());

        if(categoryExists.isPresent()) {
            throw new CategoryExistsException();
        }

        Category newCategory = new Category();

        newCategory.setName(category.name());

        newCategory = categoryRepository.save(newCategory);

        return newCategory;
    }

    public void update(UUID id, UpdateCategoryRequest request) {
        Category category = this.findById(id);

        if(!category.getName().equals(request.name())) {
            categoryRepository.findByName(request.name())
                    .ifPresent(c -> {
                        throw new CategoryExistsException();
                    });

            category.setName(request.name());
        }

        categoryRepository.save(category);
    }

    public void delete(UUID id) {
       Category category = this.findById(id);

       categoryRepository.delete(category);
    }
}
