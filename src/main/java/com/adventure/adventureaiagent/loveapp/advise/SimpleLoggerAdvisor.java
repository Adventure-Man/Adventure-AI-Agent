package com.adventure.adventureaiagent.loveapp.advise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

/**
 * @author Adventure
 * @date 2026/4/5
 * @description TODO
 */
public class SimpleLoggerAdvisor implements CallAdvisor, StreamAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(SimpleLoggerAdvisor.class);

    private void logRequest(ChatClientRequest request) {
        // 记录请求
        logger.debug("request: {}", request);
    }

    private void logResponse(ChatClientResponse chatClientResponse) {
        // 记录响应
        logger.debug("response: {}", chatClientResponse);
    }
    // 实现方法...


    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        // 记录请求
        logRequest(chatClientRequest);

        // 继续处理请求
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        // 记录响应
        logResponse(chatClientResponse);

        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        logRequest(chatClientRequest);

        Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

        return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
    }



    @Override
    public String getName() {
        return "自定义日志advisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

