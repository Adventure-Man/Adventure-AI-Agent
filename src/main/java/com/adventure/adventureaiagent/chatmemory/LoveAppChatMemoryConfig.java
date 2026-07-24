package com.adventure.adventureaiagent.chatmemory;

import com.adventure.adventureaiagent.common.constant.FileConstant;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Adventure
 * @date 2026/4/22
 * @description 聊天记忆配置
 */
@Configuration
public class LoveAppChatMemoryConfig {

    @Bean(name = "inMemoryChatMemory")
    public ChatMemory inMemoryChatMemory() {
        ChatMemoryRepository inMemoryChatMemoryRepository = new InMemoryChatMemoryRepository();
        return MessageWindowChatMemory.builder().chatMemoryRepository(inMemoryChatMemoryRepository).maxMessages(10).build();
    }

    @Bean(name = "fileBasedChatMemory")
    public ChatMemory fileBasedChatMemory() {
        return new FileBasedChatMemory(FileConstant.FILE_SAVE_DIR + "/chatMemory", 10);
    }

}
