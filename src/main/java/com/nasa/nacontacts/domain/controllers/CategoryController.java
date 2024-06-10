package com.nasa.nacontacts.domain.controllers;

import com.nasa.nacontacts.domain.Entities.Category;
import com.nasa.nacontacts.domain.dtos.CategoryDTO;
import com.nasa.nacontacts.domain.dtos.ListCategoryDTO;
import com.nasa.nacontacts.domain.dtos.request.CreateCategoryRequest;
import com.nasa.nacontacts.domain.dtos.request.UpdateCategoryRequest;
import com.nasa.nacontacts.domain.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Category", description = "Category management API")
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

    @Operation(
            summary = "Retrieve all Category",
            description = "Get a Categories array"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = { @Content(
                            schema = @Schema(implementation = ListCategoryDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)}
            ),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content),
    })
    @GetMapping
    public ResponseEntity<ListCategoryDTO> list(
//            @PageableDefault(page=0, size=10, sort = "name") Pageable pageable
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "orderBy", defaultValue = "asc") String orderBy,
            @RequestParam(required = false) String search
    ) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(orderBy)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "name"));

        Page<Category> categories = categoryService.list(pageable, search);

        ListCategoryDTO categoriesDTO = ListCategoryDTO.from(categories);

        return ResponseEntity.ok().body(categoriesDTO);
    }

    @Operation(
            summary = "Retrieve a Category by id" ,
            description = "Get a Category by id. The response is object of the CategoryDTO schema type"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {@Content(
                            schema = @Schema(implementation = CategoryDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )}
            ),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> show(@PathVariable UUID id) {
        Category category = categoryService.findById(id);

        return ResponseEntity.ok().body(CategoryDTO.from(category));
    }

    @Operation(
            summary = "Create a Category",
            description = "Create a new Category by passing in a JSON representation of the CreateCategoryRequest schema type"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {@Content(
                            schema = @Schema(implementation = CategoryDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )}
            ),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content),
    })
    @PostMapping
    public ResponseEntity<CategoryDTO> create(@RequestBody @Validated CreateCategoryRequest request) {

        Category newCategory = categoryService.create(request);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(newCategory.getId()).toUri();


        return ResponseEntity.created(uri).body(CategoryDTO.from(newCategory));
    }

    @Operation(
            summary = "Update a Category by id",
            description = "Update a Category by id passing JSON representation of the UpdatedCategoryRequest schema type"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content),
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id,
                                              @RequestBody @Validated UpdateCategoryRequest request) {

       categoryService.update(id, request);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete a Category by id",
            description = "Delete a Category by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
