package com.mks.ai.dto;

import java.util.UUID;

public record IngestResponse(
        UUID documentId,
        String filename,
        int chunkCount,
        String status
) {}
