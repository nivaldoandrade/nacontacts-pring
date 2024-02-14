package com.nasa.nacontacts.domain.services;


import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.dtos.request.CreateCategoryRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateCategoryRequest;
import com.nasa.nacontacts.domain.exceptions.CategoryExistsException;
import com.nasa.nacontacts.domain.exceptions.EntityNotFoundException;
import com.nasa.nacontacts.domain.repositories.CategoryRepository;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @InjectMocks
    CategoryService categoryService;

    @Mock
    CategoryRepository categoryRepository;


    @Test
    void shouldShowListCategories() {
        Category facebook = new Category(null, "Facebook");
        Category twitter = new Category(null, "Twitter");

        List<Category> categories = new ArrayList<>(Arrays.asList(facebook, twitter));

        when(categoryRepository.findAll()).thenReturn(categories);

        List<Category> categoriesReturn = categoryService.list();

        assertEquals(categories, categoriesReturn);
        verify(categoryRepository).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldFindByIdCategory() {
        UUID id = UUID.randomUUID();
        Category mockCategory = new Category(id, "Facebook");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(mockCategory));

        Category categoryReturn = categoryService.findById(id);

       assertEquals(mockCategory, categoryReturn);

        verify(categoryRepository).findById(id);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowErrorWhenCategoryIdNotFound() {
        UUID id = UUID.randomUUID();
        Category mockCategory = new Category(id, "Facebook");

        when(categoryRepository.findById(id)).thenThrow(new EntityNotFoundException(id, Category.class));

        EntityNotFoundException e = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.findById(id)
        );

        assertThat(e.getMessage(), is(Category.class.getSimpleName() + " with id = " + id + " not found"));
        verify(categoryRepository).findById(id);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldCreateNewCategory() {
        Category mockCategory = new Category(null, "Facebook");

        when(categoryRepository.save(mockCategory)).thenReturn(mockCategory);

        CreateCategoryRequest createCategoryRequest = CreateCategoryRequest.fromCategory(mockCategory);

        Category categoryReturn = categoryService.create(createCategoryRequest);

        assertEquals(categoryReturn, mockCategory);
        verify(categoryRepository).save(mockCategory);
    }

    @Test
    void shouldThrowErrorWhenCreatingCategoryExists() {
        String categoryName = "Facebook";
        Category category = new Category(null, "Facebook");

        CreateCategoryRequest mockRequest = CreateCategoryRequest.fromCategory(category);

        when(categoryRepository.findByName(categoryName)).thenThrow(new CategoryExistsException());

        CategoryExistsException e = assertThrows(
                CategoryExistsException.class,
                () -> categoryService.create(mockRequest)
        );

        String errorMessage = "The category is already exists";

        assertEquals(e.getMessage(), errorMessage);
        verify(categoryRepository).findByName(categoryName);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void ShouldUpdateCategory() {
        UUID id = UUID.randomUUID();

        Category requestCategory = new Category(null, "Facebook+1");
        Category existingCategory = new Category(id,"Facebook");

        existingCategory.setName(requestCategory.getName());

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));

        UpdateCategoryRequest updateCategoryRequest = UpdateCategoryRequest.fromCategory(requestCategory);

        categoryService.update(id, updateCategoryRequest);

        assertThat(existingCategory.getName(), is(requestCategory.getName()));

        verify(categoryRepository).save(existingCategory);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowErrorWhenUpdatingCategory() {
        UUID id = UUID.randomUUID();
        Category requestCategory = new Category(null, "Facebook");

        when(categoryRepository.findById(id)).thenThrow(new EntityNotFoundException(id, Category.class));

        UpdateCategoryRequest updateCategoryRequest = UpdateCategoryRequest.fromCategory(requestCategory);

        EntityNotFoundException e = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.update(id, updateCategoryRequest)
        );

        assertThat(e.getMessage(), is(Category.class.getSimpleName() + " with id = " + id + " not found"));
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowErrorWhenUpdatingCategoryWithNameAlreadyInUse() {
        UUID id = UUID.randomUUID();
        Category existsCategory = new Category(id, "Facebook");
        String mockNameRequest = "Twitter";


        when(categoryRepository.findById(id)).thenReturn(Optional.of(existsCategory));
        when(categoryRepository.findByName(mockNameRequest)).thenThrow(new CategoryExistsException());

        UpdateCategoryRequest mockRequest = UpdateCategoryRequest
                .fromCategory(new Category(null, mockNameRequest));

        CategoryExistsException e = assertThrows(
                CategoryExistsException.class,
                () -> categoryService.update(id, mockRequest)
        );

        String errorMessage = "The category is already exists";

        assertEquals(e.getMessage(), errorMessage);
        verify(categoryRepository).findById(id);
        verify(categoryRepository).findByName(mockNameRequest);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void ShouldDeleteCategory() {
        UUID id = UUID.randomUUID();
        Category existingCategory = new Category(id, "Facebook");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));

        categoryService.delete(id);

        verify(categoryRepository).delete(existingCategory);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void ShouldThrowErrorWhenDeleteCategory() {
        UUID id = UUID.randomUUID();
        Category existingCategory = new Category(id, "Facebook");

        when(categoryRepository.findById(id)).thenThrow(new EntityNotFoundException(id, Category.class));

       EntityNotFoundException e = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.delete(id)
        );

        assertThat(e.getMessage(), is(Category.class.getSimpleName() + " with id = " + id + " not found"));

        verifyNoMoreInteractions(categoryRepository);
    }
}
