package com.mks.ai.service;

import com.mks.ai.config.RagProperties;
import com.mks.ai.domain.DocumentMetadata;
import com.mks.ai.dto.IngestResponse;
import com.mks.ai.exception.UnsupportedFileTypeException;
import com.mks.ai.repository.DocumentMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;
    private final DocumentMetadataRepository metadataRepository;
    private final RagProperties props;
    private final TokenTextSplitter splitter;

    public IngestionService(VectorStore vectorStore,
                            DocumentMetadataRepository metadataRepository,
                            RagProperties props) {
        this.vectorStore = vectorStore;
        this.metadataRepository = metadataRepository;
        this.props = props;
        this.splitter =  TokenTextSplitter.builder()
        	    .withChunkSize(props.chunkSize())
        	    .withMinChunkSizeChars(350)
        	    .withMinChunkLengthToEmbed(5)
        	    .withMaxNumChunks(10000)
        	    .withKeepSeparator(true)
        	    .build();
    }

    @Transactional
    public IngestResponse ingest(MultipartFile file) {
        validate(file);

        DocumentMetadata meta = metadataRepository.save(
                new DocumentMetadata(safeName(file), file.getContentType(), file.getSize()));

        try {
            Resource resource = toResource(file);
            List<Document> rawDocs = new TikaDocumentReader(resource).get();

            // Attach source metadata BEFORE splitting so every chunk inherits it
            rawDocs.forEach(doc -> doc.getMetadata().putAll(Map.of(
                    "document_id", meta.getId().toString(),
                    "filename", meta.getFilename()
            )));

            List<Document> chunks = splitter.apply(rawDocs);
            if (chunks.isEmpty()) {
                throw new IllegalStateException("No extractable text found in file");
            }

            vectorStore.add(chunks);   // embeds + persists to pgvector

            meta.setChunkCount(chunks.size());
            meta.setStatus(DocumentMetadata.Status.READY);
            log.info("Ingested '{}' ({} chunks) as document {}",
                    meta.getFilename(), chunks.size(), meta.getId());

            return new IngestResponse(meta.getId(), meta.getFilename(),
                    chunks.size(), meta.getStatus().name());

        } catch (RuntimeException ex) {
            meta.setStatus(DocumentMetadata.Status.FAILED);
            log.error("Ingestion failed for '{}': {}", meta.getFilename(), ex.getMessage(), ex);
            throw ex;
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        String ct = file.getContentType();
        if (ct == null || !props.allowedContentTypes().contains(ct)) {
            throw new UnsupportedFileTypeException(ct, props.allowedContentTypes());
        }
    }

    private Resource toResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return safeName(file);
                }
            };
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read uploaded file", e);
        }
    }

    private String safeName(MultipartFile file) {
        String name = file.getOriginalFilename();
        return (name == null || name.isBlank()) ? "upload-" + UUID.randomUUID() : name;
    }
}
