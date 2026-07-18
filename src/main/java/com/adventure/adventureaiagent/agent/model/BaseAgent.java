package com.adventure.adventureaiagent.agent.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    private String name;

    private String systemPrompt;

    private String nextStepPrompt;

    private AgentState state = AgentState.IDLE;

    private int currentStep = 0;

    private int maxStep = 10;

    private ChatClient chatClient;

    private List<Message> messageList = new ArrayList<>();

    public String run(String userInput) {
        log.info("开始执行智能体");
        if (state != AgentState.IDLE) {
            log.error("智能体正在运行中，请勿重复执行");
            throw new RuntimeException("智能体正在运行中，请勿重复执行");
        }
        if (userInput == null || userInput.isEmpty()) {
            log.error("用户输入为空");
            throw new RuntimeException("用户输入为空");
        }
        state = AgentState.RUNNING;
        messageList.add(new UserMessage(userInput));
        List<String> resultList = new ArrayList<>();
        try {
            for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("执行步骤：{}/{}", stepNumber, maxStep);
                List<String> stepOutputs = step();
                for (String output : stepOutputs) {
                    resultList.add(output);
                }
            }
            if (currentStep >= maxStep && state != AgentState.FINISHED) {
                state = AgentState.FINISHED;
                resultList.add("已达到最大执行步数（" + maxStep + "）");
            }
            return String.join("\n\n", resultList);
        } catch (Exception e) {
            log.error("执行错误", e);
            state = AgentState.ERROR;
            return "执行错误";
        } finally {
            cleanup();
        }
    }

    public SseEmitter runStream(String message) {
        SseEmitter sseEmitter = new SseEmitter(300000L);
        AtomicBoolean completed = new AtomicBoolean(false);

        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始执行智能体");
                if (state != AgentState.IDLE) {
                    sendSafe(sseEmitter, "error: 无法从该状态运行智能体:" + this.state);
                    completeSafe(sseEmitter, completed);
                    return;
                }
                if (message == null || message.isEmpty()) {
                    sendSafe(sseEmitter, "error: 空提示词");
                    completeSafe(sseEmitter, completed);
                    return;
                }
                state = AgentState.RUNNING;
                messageList.add(new UserMessage(message));
                try {
                    for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("执行步骤：{}/{}", stepNumber, maxStep);
                        List<String> stepOutputs = step();
                        for (String output : stepOutputs) {
                            if (output != null && !output.isBlank()) {
                                sendSafe(sseEmitter, output);
                            }
                        }
                    }
                    if (currentStep >= maxStep && state != AgentState.FINISHED) {
                        state = AgentState.FINISHED;
                        sendSafe(sseEmitter, "【回答】\n已达到最大执行步数（" + maxStep + "）");
                    }
                    if (state == AgentState.FINISHED || state == AgentState.RUNNING) {
                        state = AgentState.FINISHED;
                        try {
                            sseEmitter.send(SseEmitter.event().name("done").data("ok"));
                        } catch (Exception ignored) {
                            // 客户端可能已断开
                        }
                    }
                    completeSafe(sseEmitter, completed);
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("智能体执行错误", e);
                    try {
                        sendSafe(sseEmitter, "error: 运行错误:" + e.getMessage());
                        completeSafe(sseEmitter, completed);
                    } catch (Exception ex) {
                        log.error("智能体错误收尾失败", ex);
                        if (completed.compareAndSet(false, true)) {
                            sseEmitter.completeWithError(ex);
                        }
                    }
                } finally {
                    cleanup();
                }
            } catch (Exception e) {
                log.error("智能体异步执行失败", e);
                if (completed.compareAndSet(false, true)) {
                    sseEmitter.completeWithError(e);
                }
            }
        });

        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.info("SSE connection Timeout");
        });
        sseEmitter.onCompletion(() -> {
            completed.set(true);
            if (state == AgentState.RUNNING) {
                state = AgentState.FINISHED;
            }
            cleanup();
            log.info("SSE connection completed");
        });
        sseEmitter.onError(ex -> {
            completed.set(true);
            this.state = AgentState.ERROR;
            log.warn("SSE connection error: {}", ex.getMessage());
        });
        return sseEmitter;
    }

    private void sendSafe(SseEmitter sseEmitter, String data) {
        try {
            sseEmitter.send(SseEmitter.event().data(data));
        } catch (Exception e) {
            log.warn("SSE 发送失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void completeSafe(SseEmitter sseEmitter, AtomicBoolean completed) {
        if (completed.compareAndSet(false, true)) {
            try {
                sseEmitter.complete();
            } catch (Exception e) {
                log.warn("SSE complete 失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 执行单步，返回按顺序展示给用户的文本片段（思考 / 工具执行 / 回答）。
     */
    public abstract List<String> step();

    public void cleanup() {
        log.info("清理资源");
    }
}
