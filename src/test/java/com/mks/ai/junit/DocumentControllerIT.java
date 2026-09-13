package com.mks.ai.junit;


import com.mks.ai.dto.IngestResponse;
import com.mks.ai.exception.UnsupportedFileTypeException;
import com.mks.ai.restapi.DocumentController;
import com.mks.ai.service.IngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest(DocumentController.class)
class DocumentControllerIT {

    @Autowired MockMvc mockMvc;
    @MockitoBean IngestionService ingestionService;

    @Test
    void uploadReturns201WithMetadata() throws Exception {
        var file = new MockMultipartFile("file", "notes.txt",
                MediaType.TEXT_PLAIN_VALUE, "hello world".getBytes());
        when(ingestionService.ingest(any()))
                .thenReturn(new IngestResponse(UUID.randomUUID(), "notes.txt", 3, "READY"));

        mockMvc.perform(multipart("/api/v1/documents").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("notes.txt"))
                .andExpect(jsonPath("$.chunkCount").value(3))
                .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    void unsupportedTypeReturns415() throws Exception {
        var file = new MockMultipartFile("file", "bad.exe",
                "application/octet-stream", new byte[]{1, 2, 3});
        when(ingestionService.ingest(any()))
                .thenThrow(new UnsupportedFileTypeException("application/octet-stream", List.of("text/plain")));

        mockMvc.perform(multipart("/api/v1/documents").file(file))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }
}

