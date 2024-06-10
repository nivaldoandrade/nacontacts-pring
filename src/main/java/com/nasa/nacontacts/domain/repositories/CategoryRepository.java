package com.nasa.nacontacts.domain.repositories;

import com.nasa.nacontacts.domain.Entities.Category;
import jakarta.validation.constraints.Null;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {


    @Query(value = "SELECT * FROM category c " +
            "WHERE UNACCENT(LOWER(c.name)) " +
            "LIKE CONCAT('%', LOWER(:search), '%') " +
            "OR :search IS NULL",
            nativeQuery = true
    )
    Page<Category> findAll(@Null @Param("search") String search, Pageable pageable);
    Optional<Category> findByName(String name);
}
