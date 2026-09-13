package com.mks.ai.restapi;


import com.mks.ai.dto.IngestResponse;
import com.mks.ai.service.IngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Upload and ingest documents into the knowledge base")
public class DocumentController {

    private final IngestionService ingestionService;

    public DocumentController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Operation(summary = "Upload a document (PDF, TXT, MD, DOCX) for ingestion")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngestResponse> upload(@RequestParam("file") MultipartFile file) {
        IngestResponse response = ingestionService.ingest(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> pingApps() {
        return ResponseEntity.status(HttpStatus.OK).body("App is up and running");
    }
}

