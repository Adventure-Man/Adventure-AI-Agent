package com.adventure.adventureaiagent.common.aspect;

import com.adventure.adventureaiagent.common.annotation.RateLimit;
import com.adventure.adventureaiagent.common.enums.ErrorCode;
import com.adventure.adventureaiagent.common.exception.BusinessException;
import com.adventure.adventureaiagent.common.ratelimit.RedisRateLimitService;
import com.adventure.adventureaiagent.common.utils.IpHelperUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 限流切面：拦截带 {@link RateLimit} 的方法，按 IP + chatId 做三级限流。
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Resource
    private RedisRateLimitService redisRateLimitService;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String ip = resolveIp();
        String chatId = resolveChatId(joinPoint);
        if (!StringUtils.hasText(chatId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "chatId 不能为空");
        }

        String bizKey = StringUtils.hasText(rateLimit.key())
                ? rateLimit.key()
                : joinPoint.getSignature().toShortString();

        boolean allowed = redisRateLimitService.tryAcquire(
                bizKey,
                ip,
                chatId,
                rateLimit.minuteLimit(),
                rateLimit.dayLimit(),
                rateLimit.monthLimit()
        );
        if (!allowed) {
            throw new BusinessException(
                    ErrorCode.TOO_MANY_REQUESTS,
                    String.format("访问过于频繁，每分钟最多%d次 / 每天最多%d次 / 每月最多%d次",
                            rateLimit.minuteLimit(), rateLimit.dayLimit(), rateLimit.monthLimit())
            );
        }
        return joinPoint.proceed();
    }

    private String resolveIp() {
        try {
            String ip = IpHelperUtils.getIpAddr();
            return StringUtils.hasText(ip) ? ip : "unknown";
        } catch (Exception e) {
            log.warn("获取客户端 IP 失败，降级为 unknown: {}", e.getMessage());
            return "unknown";
        }
    }

    private String resolveChatId(ProceedingJoinPoint joinPoint) {
        String chatId = resolveChatIdFromArgs(joinPoint);
        if (StringUtils.hasText(chatId)) {
            return chatId;
        }
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getParameter("chatId");
        }
        return null;
    }

    private String resolveChatIdFromArgs(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] parameterNames = signature.getParameterNames();
        if (parameterNames == null || parameterNames.length == 0) {
            parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        }
        Object[] args = joinPoint.getArgs();
        if (parameterNames == null || args == null) {
            return null;
        }
        for (int i = 0; i < parameterNames.length && i < args.length; i++) {
            if ("chatId".equals(parameterNames[i]) && args[i] != null) {
                return String.valueOf(args[i]);
            }
        }
        return null;
    }
}
