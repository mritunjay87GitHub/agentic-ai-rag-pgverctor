package com.mks.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mks.ai.domain.DocumentMetadata;

import java.util.UUID;

public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, UUID> {
}
