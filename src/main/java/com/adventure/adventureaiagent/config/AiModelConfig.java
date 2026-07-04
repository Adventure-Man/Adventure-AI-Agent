package com.adventure.adventureaiagent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
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
    public ChatClient chatClient(ChatModel primaryChatModel) {
        return ChatClient.create(primaryChatModel);
    }
}