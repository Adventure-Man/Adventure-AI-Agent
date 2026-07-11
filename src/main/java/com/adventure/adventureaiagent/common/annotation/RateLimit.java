package com.adventure.adventureaiagent.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解：按 IP + chatId 限制分钟/天/月调用次数。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 业务前缀，用于区分不同接口的 Redis key。
     */
    String key() default "";

    /**
     * 每分钟最大调用次数。
     */
    int minuteLimit() default 5;

    /**
     * 每天最大调用次数。
     */
    int dayLimit() default 20;

    /**
     * 每月最大调用次数。
     */
    int monthLimit() default 100;
}
