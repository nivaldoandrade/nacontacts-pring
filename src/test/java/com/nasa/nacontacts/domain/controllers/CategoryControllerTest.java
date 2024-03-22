package com.nasa.nacontacts.domain.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.dtos.CategoryDTO;
import com.nasa.nacontacts.domain.dtos.ListCategoryDTO;
import com.nasa.nacontacts.domain.dtos.request.CreateCategoryRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateCategoryRequest;
import com.nasa.nacontacts.domain.exceptions.EntityNotFoundException;
import com.nasa.nacontacts.domain.exceptions.GlobalExceptionHandler;
import com.nasa.nacontacts.domain.services.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @InjectMocks
    CategoryController categoryController;

    @Mock
    CategoryService categoryService;

    private MockMvc mockMvc;

    private String json;

    private String url;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .alwaysDo(print()).build();
        url = "/categories";
    }

    @Test
    void shouldShowListCategories() throws Exception {
        Category category = new Category(UUID.randomUUID(), "Facebook");

        Page<Category> categories = new PageImpl<>(List.of(category));

        when(categoryService.list(any(Pageable.class))).thenReturn(categories);

        String expectedCategories = objectMapper.writeValueAsString(ListCategoryDTO.from(categories));

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCategories));

/*
        String responseContent = result.andReturn().getResponse().getContentAsString();

        List<CategoryDTO> returnedCategories = objectMapper.readValue(responseContent,
                new TypeReference<List<CategoryDTO>>() {});


        assertTrue(returnedCategories.contains(categoryDTO));
*/

        verify(categoryService).list(any(Pageable.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void shouldShowAscendingListCategories() throws Exception {
        Category category = new Category(UUID.randomUUID(), "Facebook");

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);

        Page<Category> categories = new PageImpl<>(List.of(category),pageable, 1);

        when(categoryService.list(pageable)).thenReturn(categories);

        String expectedCategories = objectMapper.writeValueAsString(ListCategoryDTO.from(categories));

        mockMvc.perform(get(url)
                .param("orderBy", "asc")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCategories));

        verify(categoryService).list(pageable);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void shouldShowDescendingListCategories() throws Exception {
        Category category = new Category(UUID.randomUUID(), "Facebook");

        Sort sort = Sort.by(Sort.Direction.DESC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);

        Page<Category> categories = new PageImpl<>(List.of(category), pageable, 1);

        when(categoryService.list(pageable)).thenReturn(categories);

        String expectedCategories = objectMapper.writeValueAsString(ListCategoryDTO.from(categories));

        mockMvc.perform(get(url)
                .param("orderBy", "desc")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCategories));

        verify(categoryService).list(pageable);
        verifyNoMoreInteractions(categoryService);

    }

    @Test
    void shouldShowEmptyListCategories() throws Exception {
        Page<Category> categories = new PageImpl<>(Collections.emptyList());

        when(categoryService.list(any(Pageable.class))).thenReturn(categories);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .param("orderBy", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));

        verify(categoryService).list(any(Pageable.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void ShouldShowCategoryById() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Facebook");
        CategoryDTO categoryDTO = CategoryDTO.from(category);
        json = objectMapper.writeValueAsString(categoryDTO);

        when(categoryService.findById(id)).thenReturn(category);

        mockMvc.perform(get(url + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param("id", String.valueOf(id))
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryDTO.id().toString()))
                .andExpect(jsonPath("$.name").value(categoryDTO.name()));

        verify(categoryService).findById(id);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void shouldGet404WhenCategoryNoExists() throws Exception {
        UUID id = UUID.randomUUID();

        given(categoryService.findById(id)).willThrow(new EntityNotFoundException(id, Category.class));

        String message = Category.class.getSimpleName() + " with id = " + id + " not found";

        mockMvc.perform(get(url + "/" + id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.status").value(404));


        verify(categoryService).findById(id);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void shouldCreateCategory() throws Exception{
        UUID id = UUID.randomUUID();
        Category category = new Category(null, "Facebook");
        CreateCategoryRequest mockRequest = CreateCategoryRequest.fromCategory(category);
        json = objectMapper.writeValueAsString(mockRequest);
        category.setId(id);
        CategoryDTO categoryDTO = CategoryDTO.from(category);

        when(categoryService.create(mockRequest)).thenReturn(category);

        String responseLocation = "http://localhost/categories/" + id;

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryDTO.id().toString()))
                .andExpect(jsonPath("$.name").value(categoryDTO.name()))
                .andExpect(header().string("Location", responseLocation));

        verify(categoryService).create(mockRequest);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void shouldThrowErrorWhenCreateCategoryWithInvalidData() throws Exception {
        Category category = new Category(null, null);
        CreateCategoryRequest createCategoryRequest = CreateCategoryRequest.fromCategory(category);

        json = objectMapper.writeValueAsString(createCategoryRequest);

        mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"));

        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldUpdateCategoryById() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = new Category(null, "Facebook");
        UpdateCategoryRequest mockRequest = UpdateCategoryRequest.fromCategory(category);
        category.setId(id);

        json = objectMapper.writeValueAsString(mockRequest);

        mockMvc.perform(put(url + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());

        verify(categoryService).update(id, mockRequest);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void shouldThrowErrorWhenUpdatingNonexistentCategoryById() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = new Category(null, "Facebook");
        UpdateCategoryRequest mockRequest = UpdateCategoryRequest.fromCategory(category);

        doThrow(new EntityNotFoundException(id, Category.class)).when(categoryService).update(id, mockRequest);

        json = objectMapper.writeValueAsString(mockRequest);

        mockMvc.perform(put(url + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());

        verify(categoryService).update(id, mockRequest);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void shouldThrowErrorUpdatingCategoryWithInvalidData() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = new Category(null, null);
        UpdateCategoryRequest mockRequest = UpdateCategoryRequest.fromCategory(category);

        json = objectMapper.writeValueAsString(mockRequest);

        mockMvc.perform(put(url + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Name is required"));

        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldDeleteCategoryById() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete(url + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param("id", String.valueOf(id)))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(id);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void shouldThrowErrorWhenDeletingNonexistentCategoryById() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new EntityNotFoundException(id, Category.class)).when(categoryService).delete(id);

        String message = Category.class.getSimpleName() + " with id = " + id + " not found";

        mockMvc.perform(delete(url + "/" + id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(message));


        verify(categoryService).delete(id);
        verifyNoMoreInteractions(categoryService);
    }

}
