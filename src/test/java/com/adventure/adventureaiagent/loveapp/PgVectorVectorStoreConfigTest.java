package com.adventure.adventureaiagent.loveapp;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class PgVectorVectorStoreConfigTest {

    @Resource
    VectorStore vectorStore;

    @Resource
    EmbeddingModel embeddingModel;

    @Test
    void test() {
        float[] embedding = embeddingModel.embed("测试文本");
        //当前模型输出维度
        System.out.println(embedding);
        System.out.println("当前模型输出维度: " + embedding.length);

        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
        // 添加文档
        vectorStore.add(documents);
        // 相似度查询
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("Corner").topK(1).build());
        System.out.println( results);
        Assertions.assertNotNull(results);
    }
}
