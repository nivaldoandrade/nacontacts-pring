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
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        Page<Category> categories = new PageImpl<>(List.of(facebook, twitter));

        when(categoryRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(categories);

        Page<Category> categoriesReturn = categoryService.list(Pageable.unpaged(), "");

        assertEquals(categories, categoriesReturn);
        verify(categoryRepository).findAll(any(String.class), any(Pageable.class));
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldShowAscendingListCategories() {
        Category facebook = new Category(null, "Facebook");
        Category twitter = new Category(null, "Twitter");

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(0, 2, sort);

        Page<Category> categories = new PageImpl<>(List.of(facebook, twitter));

        when(categoryRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(categories);

        List<Category> expectedCategories = List.of(facebook, twitter);

        Page<Category> categoriesReturn = categoryService.list(pageable,"");

        assertEquals(expectedCategories, categoriesReturn.toList());
        assertEquals(categories.getSort(), categoriesReturn.getSort());
        assertEquals(categories.getSize(), categoriesReturn.getSize());
        assertEquals(categories.getTotalPages(), categoriesReturn.getTotalPages());

        verify(categoryRepository).findAll(any(String.class), eq(pageable));
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldShowDescendingListCategories() {
        Category facebook = new Category(null, "Facebook");
        Category twitter = new Category(null, "Twitter");

        Sort sort = Sort.by(Sort.Direction.DESC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Category> categories = new PageImpl<>(List.of(twitter, facebook));

        when(categoryRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(categories);

        List<Category> expectedCategories = List.of(twitter, facebook);

        Page<Category> categoriesReturn = categoryService.list(pageable, "");

        assertEquals(expectedCategories, categoriesReturn.toList());
        assertEquals(categories.getSort(), categoriesReturn.getSort());
        assertEquals(categories.getSize(), categoriesReturn.getSize());
        assertEquals(categories.getTotalPages(), categoriesReturn.getTotalPages());

        verify(categoryRepository).findAll(any(String.class), eq(pageable));
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldShowFilteredListCategories() {
        Category category = new Category(UUID.randomUUID(), "category 1");

        Page<Category> categories = new PageImpl<>(List.of(category));

        when(categoryRepository.findAll(any(String.class), any(Pageable.class))).thenReturn(categories);

        List<Category> expectedCategories = List.of(category);

        Page<Category> categoriesReturn = categoryService.list(Pageable.unpaged(), "category 1");

        assertEquals(expectedCategories, categoriesReturn.toList());
        assertEquals(categories.getSize(), categoriesReturn.getSize());

        verify(categoryRepository).findAll(any(String.class), any(Pageable.class));
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
        Category category = new Category(null, categoryName);
        Category categoryExists = mock(Category.class);
        categoryExists.setName(categoryName);
        CreateCategoryRequest mockRequest = CreateCategoryRequest.fromCategory(category);

        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.of(categoryExists));

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

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByName(any(String.class)))
                .thenReturn(Optional.empty());

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
        Category category = new Category(id, "Facebook");

        String categoryNameExists = "Twitter";
        Category mockedCategory = mock(Category.class);
        mockedCategory.setName(categoryNameExists);

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName(categoryNameExists))
                .thenReturn(Optional.of(mockedCategory));

        UpdateCategoryRequest mockRequest = UpdateCategoryRequest
                .fromCategory(new Category(null, categoryNameExists));

        CategoryExistsException e = assertThrows(
                CategoryExistsException.class,
                () -> categoryService.update(id, mockRequest)
        );

        String errorMessage = "The category is already exists";

        assertEquals(e.getMessage(), errorMessage);
        verify(categoryRepository).findById(id);
        verify(categoryRepository).findByName(categoryNameExists);
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
