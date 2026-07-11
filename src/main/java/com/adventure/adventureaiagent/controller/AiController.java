package com.adventure.adventureaiagent.controller;

import com.adventure.adventureaiagent.agent.model.AdventureManus;
import com.adventure.adventureaiagent.common.annotation.RateLimit;
import com.adventure.adventureaiagent.common.resp.BaseResponse;
import com.adventure.adventureaiagent.common.utils.ResultUtils;
import com.adventure.adventureaiagent.loveapp.LoveApp;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel chatModel;

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChatWithMemory(message, chatId);
    }

    /**
     * 聊天接口 添加sse对应的MediaType
     *
     * @param message
     * @param chatId
     * @return
     */
//    @RateLimit(key = "love_app_sse", minuteLimit = 5, dayLimit = 20, monthLimit = 100)
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(@RequestParam String message, @RequestParam String chatId) {
        return loveApp.doChatWithModelSse(message, chatId);
    }

    /**
     * 聊天接口 省略MediaType 添加ServerSentEvent
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse1")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppSSE1(String message, String chatId) {
        Flux<ServerSentEvent<String>> map = loveApp.doChatWithModelSse(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .event("message")
                        .build())
                .onErrorResume(error -> {
                    log.error("SSE聊天错误: {}", error.getMessage(), error);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .data("{" + "error\":\"" + error.getMessage() + "\"}")
                            .event("error")
                            .build());
                });
        return map;
    }

    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时
        // 获取 Flux 数据流并直接订阅
        loveApp.doChatWithModelSse(message, chatId)
                .subscribe(
                        // 处理每条消息
                        chunk -> {
                            try {
                                // 发送消息到客户端
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        emitter::completeWithError,
                        // 处理完成
                        emitter::complete
                );
        // 返回emitter
        return emitter;
    }



    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        AdventureManus adventureManus = new AdventureManus(allTools, chatModel);
        SseEmitter sseEmitter = adventureManus.runStream(message);
        return sseEmitter;
    }

    /**
     * 获取智能体的建议
     *
     * @return
     */
    @GetMapping("/love-expert/suggestions")
    public BaseResponse<List<String>> getLoveExpertSuggestions() {
        List<String> suggestions = List.of(
                "单身如何提升自己,提高找对象的概率?",
                "男朋友最近回消息很慢，是不是不爱我了？",
                "婚后关系不太亲密,该如何做提高亲密性？");
        return ResultUtils.success(suggestions);
    }


}
