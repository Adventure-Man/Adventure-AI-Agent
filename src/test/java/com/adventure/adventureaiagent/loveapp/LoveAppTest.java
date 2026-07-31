package com.adventure.adventureaiagent.loveapp;

import cn.hutool.core.lang.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SuppressWarnings("unused")
@SpringBootTest
public class LoveAppTest {

    @Autowired
    private LoveApp loveApp;

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String message = "上海浦东新区周末约会地点推荐2个？";
        String answer =  loveApp.doChatWithMcp(message, chatId);
        System.out.println(answer);
        Assertions.assertNotNull(answer);
    }

    /**
     * 测试智谱模型---流式输出内容
     */
    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？？";
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
        Flux<String> answer = loveApp.doChatWithModelSse(message, chatId);
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

}
