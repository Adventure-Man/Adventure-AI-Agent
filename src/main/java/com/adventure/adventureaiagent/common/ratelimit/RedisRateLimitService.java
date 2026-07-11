package com.adventure.adventureaiagent.common.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * 基于 Redis + Lua 的三级窗口限流（分钟 / 天 / 月）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRateLimitService {

    private static final String KEY_PREFIX = "rate_limit";
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * 原子检查并递增三个窗口计数。
     * 返回值：1=通过；-1=分钟超限；-2=天超限；-3=月超限。
     */
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>("""
            local minuteCount = tonumber(redis.call('GET', KEYS[1]) or '0')
            local dayCount = tonumber(redis.call('GET', KEYS[2]) or '0')
            local monthCount = tonumber(redis.call('GET', KEYS[3]) or '0')

            if minuteCount >= tonumber(ARGV[4]) then
              return -1
            end
            if dayCount >= tonumber(ARGV[5]) then
              return -2
            end
            if monthCount >= tonumber(ARGV[6]) then
              return -3
            end

            minuteCount = redis.call('INCR', KEYS[1])
            if minuteCount == 1 then
              redis.call('EXPIRE', KEYS[1], ARGV[1])
            end

            dayCount = redis.call('INCR', KEYS[2])
            if dayCount == 1 then
              redis.call('EXPIRE', KEYS[2], ARGV[2])
            end

            monthCount = redis.call('INCR', KEYS[3])
            if monthCount == 1 then
              redis.call('EXPIRE', KEYS[3], ARGV[3])
            end

            return 1
            """, Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试获取一次调用配额。
     *
     * @return true 表示通过；false 表示任一窗口超限
     */
    public boolean tryAcquire(String bizKey, String ip, String chatId,
                              int minuteLimit, int dayLimit, int monthLimit) {
        String base = KEY_PREFIX + ":" + bizKey + ":" + ip + ":" + chatId;
        List<String> keys = Arrays.asList(
                base + ":minute",
                base + ":day",
                base + ":month"
        );

        long minuteTtl = 60L;
        long dayTtl = secondsUntilEndOfDay();
        long monthTtl = secondsUntilEndOfMonth();

        Long result = stringRedisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                keys,
                String.valueOf(minuteTtl),
                String.valueOf(dayTtl),
                String.valueOf(monthTtl),
                String.valueOf(minuteLimit),
                String.valueOf(dayLimit),
                String.valueOf(monthLimit)
        );

        if (result == null) {
            log.warn("Redis 限流脚本返回 null, bizKey={}, ip={}, chatId={}", bizKey, ip, chatId);
            return false;
        }
        if (result < 0) {
            log.info("限流触发 result={}, bizKey={}, ip={}, chatId={}", result, bizKey, ip, chatId);
            return false;
        }
        return true;
    }

    private long secondsUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        LocalDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay();
        return Math.max(1L, ChronoUnit.SECONDS.between(now, endOfDay));
    }

    private long secondsUntilEndOfMonth() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        LocalDateTime endOfMonth = YearMonth.from(now).plusMonths(1).atDay(1).atStartOfDay();
        return Math.max(1L, ChronoUnit.SECONDS.between(now, endOfMonth));
    }
}
