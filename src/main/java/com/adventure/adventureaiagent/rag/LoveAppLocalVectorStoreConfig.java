package com.adventure.adventureaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class LoveAppLocalVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    /**
     * 本地知识库通过EmbeddingModel向量转换 -- EmbeddingModel
     * @return
     */
    @Bean("loveAppVectorStore")
    VectorStore loveAppVectorStore(EmbeddingModel embeddingModel) throws IOException {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        // 加载文档
//        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
//        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }

//    @Bean(name = "loveAppVectorStoreZhiPuAi")
//    VectorStore loveAppVectorStoreZhiPuAi(@Qualifier("zhiPuAiEmbeddingModel") EmbeddingModel zhiPuAiEmbeddingModel) {
//        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(zhiPuAiEmbeddingModel)
//                .build();
//        // 加载文档
//        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
//        simpleVectorStore.add(documents);
//        return simpleVectorStore;
//    }
}
