package com.nasa.nacontacts.domain.config;


import com.nasa.nacontacts.domain.exceptions.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private StorageType type = StorageType.Local;

    private Local local = new Local();
    private S3 s3 = new S3();

    @Getter(AccessLevel.NONE)
    private String TempDir;
    private String cdnUrl;
    private Path tempStorageLocation;
    private Path localStorageLocation;

    @PostConstruct
    private void init() {
        Path uploadTempDirPath = Paths.get(TempDir).toAbsolutePath().normalize();
        Path uploadLocalDirPath = Paths.get(local.getUploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadTempDirPath);
            Files.createDirectories(uploadLocalDirPath);

            this.tempStorageLocation = uploadTempDirPath;
            this.localStorageLocation = uploadLocalDirPath;

        } catch(Exception e) {
            throw new FileStorageException("Error creating temp folder where files will be stored");
        }
    }

    public enum StorageType {
        Local, S3
    }

    @Getter
    @Setter
    public class S3 {
        private String bucketName;
        private String region;
        private String accessKey;
        private String secretKey;
    }

    @Getter
    @Setter
    private class Local {
        private String uploadDir;
    }
}
