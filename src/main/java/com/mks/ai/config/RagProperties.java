package com.mks.ai.config;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "rag")
public record RagProperties(
        @Min(100) int chunkSize,
        @Min(1) int topK,
        double similarityThreshold,
        @NotEmpty List<String> allowedContentTypes
) {}
