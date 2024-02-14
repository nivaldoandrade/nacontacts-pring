package com.nasa.nacontacts.domain.controllers;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.dtos.CategoryDTO;
import com.nasa.nacontacts.domain.dtos.request.CreateCategoryRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateCategoryRequest;
import com.nasa.nacontacts.domain.services.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
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
    public ResponseEntity<List<CategoryDTO>> list() {
        List<Category> categories = categoryService.list();

        List<CategoryDTO> categoriesDTO = categories.stream().map(CategoryDTO::from).toList();

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
