package com.nasa.nacontacts.domain.repositories;

import com.nasa.nacontacts.domain.Entities.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {

    Optional<Contact> findByEmail(String email);
}
