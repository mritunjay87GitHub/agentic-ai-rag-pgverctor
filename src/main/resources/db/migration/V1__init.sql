-- Required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Vector store table used by Spring AI PgVectorStore.
-- Dimension (1536) must match the embedding model (text-embedding-3-small).
CREATE TABLE IF NOT EXISTS vector_store (
    id          uuid        DEFAULT uuid_generate_v4() PRIMARY KEY,
    content     text        NOT NULL,
    metadata    jsonb,
    embedding   vector(1536) NOT NULL
);

-- Approximate nearest-neighbour index (cosine distance)
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx
    ON vector_store
    USING hnsw (embedding vector_cosine_ops);

-- Application-owned table tracking uploaded documents (not managed by Spring AI)
CREATE TABLE IF NOT EXISTS document_metadata (
    id              uuid        DEFAULT uuid_generate_v4() PRIMARY KEY,
    filename        varchar(512) NOT NULL,
    content_type    varchar(255) NOT NULL,
    size_bytes      bigint       NOT NULL,
    chunk_count     integer      NOT NULL DEFAULT 0,
    status          varchar(32)  NOT NULL DEFAULT 'PROCESSING',
    uploaded_at     timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS document_metadata_status_idx ON document_metadata (status);
