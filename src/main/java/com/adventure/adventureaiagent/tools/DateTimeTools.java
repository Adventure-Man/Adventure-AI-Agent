package com.adventure.adventureaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具：仅在用户问题涉及当前日期、星期、节假日等时间信息时调用。
 */
public class DateTimeTools {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(description = """
            获取用户时区下的当前日期（年月日，格式 yyyy-MM-dd HH:mm:ss）。
            仅在用户明确需要知道「今天几号、当前日期、星期几、是否周末/节假日」等与日期相关的问题时调用；
            若问题与日期无关（如天气、搜索、生成文件等），不要调用此工具。
            """)
    public String getCurrentDateTime() {
        ZoneId zoneId = LocaleContextHolder.getTimeZone().toZoneId();
        LocalDateTime today = LocalDateTime.now(zoneId);
        return today.format(DATE_FORMATTER);
    }
}
