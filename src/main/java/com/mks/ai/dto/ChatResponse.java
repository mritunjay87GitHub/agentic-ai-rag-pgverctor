package com.mks.ai.dto;

import java.util.List;

public record ChatResponse(
        String answer,
        boolean grounded,
        List<SourceChunk> sources
) {}

