package com.adventure.adventureaiagent.loveapp;

import cn.hutool.core.lang.UUID;
import com.adventure.adventureaiagent.apimodel.TongYiApiTest;
import io.reactivex.Emitter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;
@SuppressWarnings("unused")
@SpringBootTest
class LoveAppTest {

    @Autowired
    private LoveApp loveApp;

    /**
     * 测试智谱模型---流式输出内容
     */
    @Test
    void doChatWithZhiPuModel() {
        String chatId = UUID.randomUUID().toString();
        String message = "我刚刚问什么来着？忘记了";
        // sse 流式输出,打字机效果
        Flux<String> stringFlux = loveApp.doChatWithModelSse(message, chatId);
        stringFlux.doOnNext(System.out::println)
                .doOnError(error -> System.err.println("错误：" + error.getMessage()))
                        .doOnComplete(() -> System.out.println("\n回答完成"))
                                .blockLast();
        Assertions.assertNotNull(stringFlux);
    }

    /**
     * 测试智谱模型---非流式输出内容
     */
    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        Flux<String> answer =  loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 订阅 Flux 并输出到控制台
        answer.subscribe(
                System.out::print,
                error -> System.err.println("错误：" + error.getMessage()),
                () -> System.out.println("\n回答完成")
        );
        // 等待一段时间确保流处理完成
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 测试智谱模型---带记忆的问答
     */
    @Test
    void doChatWithMemory(){
        String chatId = UUID.randomUUID().toString();
        String message = "我的名字是小明，我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithMemory(message, chatId);
        Assertions.assertNotNull(answer);
//
//        String message1 = "你是谁？";
//        String answer1 =  loveApp.doChatWithMemory(message1, chatId);
//        Assertions.assertNotNull(answer1);

        String message2 = "我叫什么？";
        String answer2 =  loveApp.doChatWithMemory(message2, chatId);
        Assertions.assertNotNull(answer2);

    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
//        String message = "上海什么天气？";
        String answer =  loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

}