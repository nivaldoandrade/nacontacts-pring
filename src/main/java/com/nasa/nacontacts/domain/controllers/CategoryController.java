package com.nasa.nacontacts.domain.controllers;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.dtos.CategoryDTO;
import com.nasa.nacontacts.domain.dtos.ListCategoryDTO;
import com.nasa.nacontacts.domain.dtos.request.CreateCategoryRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateCategoryRequest;
import com.nasa.nacontacts.domain.services.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    /*
        Posso utilizar a anotação @Autowired ao invés de instanciar o service no construtor.
    */

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ListCategoryDTO> list(
//            @PageableDefault(page=0, size=10, sort = "name") Pageable pageable
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "orderBy", defaultValue = "asc") String orderBy
    ) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(orderBy)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "name"));

        Page<Category> categories = categoryService.list(pageable);

        ListCategoryDTO categoriesDTO = ListCategoryDTO.from(categories);

        return ResponseEntity.ok().body(categoriesDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> show(@PathVariable UUID id) {
        Category category = categoryService.findById(id);

        return ResponseEntity.ok().body(CategoryDTO.from(category));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> create(@RequestBody @Validated CreateCategoryRequest request) {

        Category newCategory = categoryService.create(request);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(newCategory.getId()).toUri();


        return ResponseEntity.created(uri).body(CategoryDTO.from(newCategory));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id,
                                              @RequestBody @Validated UpdateCategoryRequest request) {

       categoryService.update(id, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
