package com.mks.ai.dto;

public record SourceChunk(
        String documentId,
        String filename,
        Double score,
        String excerpt
) {}
