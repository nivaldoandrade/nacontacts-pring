package com.nasa.nacontacts.domain.config;

import com.nasa.nacontacts.domain.services.LocalStorageService;
import com.nasa.nacontacts.domain.services.S3StorageService;
import com.nasa.nacontacts.domain.services.StorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    public StorageService storageService(StorageProperties storageProperties, S3ClientConfig s3ClientConfig) {
        StorageProperties.StorageType storageType = storageProperties.getType();

        return switch (storageType) {
            case S3:
                yield  new S3StorageService(s3ClientConfig.s3Client(), storageProperties);
            case Local:
                yield new LocalStorageService(storageProperties);
        };

    }
}
