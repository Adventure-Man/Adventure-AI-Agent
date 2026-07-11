package com.adventure.adventureaiagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务线程池配置
 * 为 AI 推理等长时间阻塞任务提供专用线程池，避免占用 ForkJoinPool.commonPool()
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * AI 任务专用线程池
     */
    @Bean("aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                4,                          // 核心线程数
                10,                         // 最大线程数
                60,                         // 空闲线程存活时间
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),  // 工作队列
                new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略：调用者线程执行
        );
        return executor;
    }
}
