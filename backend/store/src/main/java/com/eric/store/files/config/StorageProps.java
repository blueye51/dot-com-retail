package com.eric.store.files.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "storage.upload")
public record StorageProps(
        List<String> allowedTypes,
        long maxSizeBytes
) {}

