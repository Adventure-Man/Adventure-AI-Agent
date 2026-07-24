package com.adventure.adventureaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;


/**
 * @author Adventure
 * @date 2026/5/15
 * @description pgvectorstore配置类
 */
@Slf4j
@Configuration
public class PgVectorStoreConfig {

    private static final int EMBEDDING_BATCH_SIZE = 10;

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        PgVectorStore pgVectorStore = PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(1024)                    // Optional: defaults to model dimensions or 1536
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();

        List<Document> allDocuments = loveAppDocumentLoader.loadMarkdowns();
        int totalDocuments = allDocuments.size();
        log.info("Loading {} documents into PgVectorStore", totalDocuments);

        for (int i = 0; i < totalDocuments; i += EMBEDDING_BATCH_SIZE) {
            int end = Math.min(i + EMBEDDING_BATCH_SIZE, totalDocuments);
            List<Document> batch = allDocuments.subList(i, end);
            pgVectorStore.add(batch);
            log.debug("Added documents batch [{}, {}] ({} docs) to PgVectorStore", i, end, batch.size());
        }

        log.info("Successfully added all {} documents to PgVectorStore", totalDocuments);
        return pgVectorStore;
    }
}
