package com.nasa.nacontacts.domain.repositories;

import com.nasa.nacontacts.domain.Entities.Contact;
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
public interface ContactRepository extends JpaRepository<Contact, UUID>, JpaSpecificationExecutor<Contact> {

    @Query(value = "SELECT * FROM contact c " +
            "WHERE " +
            "UNACCENT(LOWER(c.name)) LIKE CONCAT('%', LOWER(:search), '%') " +
            "OR " +
            "LOWER(c.email) LIKE CONCAT('%', LOWER(:search), '%') " +
            "OR " +
            "LOWER(c.phone) LIKE CONCAT('%', LOWER(:search), '%') " +
            "OR " +
            ":search IS NULL",
            nativeQuery = true
    )
    Page<Contact> findAll(@Param("search") String search, Pageable pageable);

    Optional<Contact> findByEmail(String email);
}
