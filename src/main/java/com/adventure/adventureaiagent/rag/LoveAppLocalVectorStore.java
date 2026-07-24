package com.adventure.adventureaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

//@Configuration
public class LoveAppLocalVectorStore {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    /**
     * SimpleVectorStore本地内存 知识库通过EmbeddingModel向量转换 -- EmbeddingModel
     * @return
     */
    @Bean("loveAppVectorStore")
    VectorStore loveAppVectorStore(EmbeddingModel embeddingModel) throws IOException {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        // 加载文档
        //List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        //simpleVectorStore.add(documents);
        return simpleVectorStore;
    }
}
