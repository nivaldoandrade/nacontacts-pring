package com.nasa.nacontacts.domain.Entities;

import com.nasa.nacontacts.domain.config.StorageProperties;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class ContactEntityListener {

    private final String cdnUrl;

    private final StorageProperties.StorageType storageType;

    public ContactEntityListener(StorageProperties storageProperties) {
        this.cdnUrl = storageProperties.getCdnUrl();
        this.storageType = storageProperties.getType();
    }


    @PostLoad
    @PostPersist
    void setPhotoUrl(Contact contact) {
        if(contact.getPhoto() == null) {
            return;
        }

        String photoUrl = switch (storageType) {
            case Local:
                yield ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("contacts/image/")
                        .path(contact.getPhoto())
                        .toUriString();
            case S3:
                yield cdnUrl + contact.getPhoto();
        };

        contact.setPhotoUrl(photoUrl);
    }
}
