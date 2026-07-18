package com.adventure.adventureaiagent.agent.model;

import io.reactivex.Completable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。  
 *   
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。  
 * 子类必须实现step方法。  
 */  
@Data
@Slf4j
public abstract class BaseAgent {
    /**
     * 智能体名称
     */
    private String name;

    /**
     * 系统提示语
     */
    private String systemPrompt;
    /**
     * 下一步提示语
     */
    private String nextStepPrompt;

    /**
     * agent状态
     */
    private AgentState state = AgentState.IDLE;

    /**
     * 智能体执行步骤控制
     */
    private int currentStep = 0;
    /**
     * 最大执行步骤
     */
    private int maxStep = 10;

    /**
     * 大模型LLM
     */
    private ChatClient chatClient;

    /**
     * 会话记忆memory(自主维护上下文会话) 观察 Observer
     */
    private List<Message>  messageList = new ArrayList<>();

    /**
     * 运行智能体
     */
    public String run(String userInput) {
        log.info("开始执行智能体");
        //1.基础校验
        if (state != AgentState.IDLE) {
            log.error("智能体正在运行中，请勿重复执行");
            throw new RuntimeException("智能体正在运行中，请勿重复执行");
        }
        if (userInput == null || userInput.isEmpty()){
            log.error("用户输入为空");
            throw new RuntimeException("用户输入为空");
        }
        state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userInput));
        // 保存结果列表()
        List<String> resultList = new ArrayList<>();
        // 2.执行步骤
        try {
            for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                int stepNumber = i++;
                currentStep = stepNumber;
                log.info("执行步骤：{}/{}", stepNumber, maxStep);
                String stepResult = step();
                resultList.add("Step " + stepNumber + ": " + stepResult);
            }
            if (currentStep >= maxStep){
                state = AgentState.FINISHED;
                resultList.add("Terminated: reach max step (" + maxStep + ")");
            }
            return String.join("\n", resultList);
        }catch (Exception e){
            log.error("执行错误",e);
            state = AgentState.ERROR;
            return "执行错误";
        }finally {
            //3.清理资源
            cleanup();
        }
    }

    public SseEmitter runStream(String message) {
        // 时间 5分钟超时
        SseEmitter sseEmitter = new SseEmitter(300000L);
        // 异步执行 防止阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始执行智能体");
                //1.基础校验
                if (state != AgentState.IDLE) {
                    sseEmitter.send("error: 无法从该状态运行智能体:"+this.state);
                    sseEmitter.complete();
                }
                if (message == null || message.isEmpty()){
                    sseEmitter.send("error: 空提示词");
                    sseEmitter.complete();
                }
                state = AgentState.RUNNING;
                // 记录消息上下文
                messageList.add(new UserMessage(message));
                // 保存结果列表
                //List<String> resultList = new ArrayList<>();
                // 2.执行步骤
                try {
                    for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                        int stepNumber = i++;
                        currentStep = stepNumber;
                        log.info("执行步骤：{}/{}", stepNumber, maxStep);
                        String stepResult = step();
                        //resultList.add("Step " + stepNumber + ": " + stepResult);
                        sseEmitter.send(stepResult);
                    }
                    if (currentStep >= maxStep){
                        state = AgentState.FINISHED;
                        sseEmitter.send("Terminated: reach max step (" + maxStep + ")");
                    }
                    sseEmitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("智能体执行错误",e);
                    try{
                        sseEmitter.send("error: 运行错误:"+e.getMessage());
                        sseEmitter.complete();
                    }catch (Exception ex){
                        log.error("智能体执行错误",ex);
                        sseEmitter.completeWithError(ex);
                    }
                } finally {
                    //3.清理资源
                    cleanup();
                }
            }catch (Exception e){
                sseEmitter.completeWithError(e);
            }
        });

        //设置超时 和 完成回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.info("SSE connection Timeout");
        });
        sseEmitter.onCompletion(() -> {
            if (state == AgentState.RUNNING){
                state = AgentState.FINISHED;
            }
            cleanup();
            log.info("SSE connection completed");
        });
        return sseEmitter;
    }
    /**
     * 抽象方法，定义智能体的执行步骤
     */
    public abstract String step();

    /**
     * 清理资源
     */
    public void cleanup(){
        // 子类可以重写清理资源
        log.info("清理资源");
    }

}
