package com.adventure.adventureaiagent.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(AiProviderProperties.class)
public class AiModelConfig {

    @Bean
    @Primary
    public ChatModel primaryChatModel(
            AiProviderProperties properties,
            @Qualifier("zhiPuAiChatModel") ChatModel zhiPuAiChatModel,
            @Qualifier("dashScopeChatModel") ChatModel dashScopeChatModel) {

        String provider = properties.getProvider();
        if ("zhipu".equalsIgnoreCase(provider)) {
            return zhiPuAiChatModel;
        }
        if ("dashscope".equalsIgnoreCase(provider)) {
            return dashScopeChatModel;
        }

        throw new IllegalArgumentException("不支持的AI提供商: " + provider);
    }

    @Bean
    @Primary
    public EmbeddingModel primaryEmbeddingModel(
            AiProviderProperties properties,
            @Qualifier("zhiPuAiEmbeddingModel") EmbeddingModel zhiPuAiEmbeddingModel,
            @Qualifier("dashscopeEmbeddingModel") EmbeddingModel dashScopeEmbeddingModel) {

        String provider = properties.getProvider();
        if ("zhipu".equalsIgnoreCase(provider)) {
            return zhiPuAiEmbeddingModel;
        }
        if ("dashscope".equalsIgnoreCase(provider)) {
            return dashScopeEmbeddingModel;
        }

        throw new IllegalArgumentException("不支持的AI提供商: " + provider);
    }

//    @Bean
//    @Primary
//    public ChatOptions primaryChatOptions(
//            AiProviderProperties properties) {
//
//        String provider = properties.getProvider();
//        if ("zhipu".equalsIgnoreCase(provider)) {
//            return new ZhiPuAiChatOptions();
//        }
//        if ("dashscope".equalsIgnoreCase(provider)) {
//            return new DashScopeChatOptions();
//        }
//
//        throw new IllegalArgumentException("不支持的AI提供商: " + provider);
//    }

    @Bean
    @Primary
    public ChatClient chatClient(ChatModel primaryChatModel) {
        return ChatClient.create(primaryChatModel);
    }
}