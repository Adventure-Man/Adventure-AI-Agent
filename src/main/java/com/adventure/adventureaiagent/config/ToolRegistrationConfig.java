package com.adventure.adventureaiagent.config;

import com.adventure.adventureaiagent.tools.DateTimeTools;
import com.adventure.adventureaiagent.tools.FileOperationTool;
import com.adventure.adventureaiagent.tools.WeatherTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Adventure
 * @date 2026/4/2
 * @description 工具注册配置
 */
@Configuration
public class ToolRegistrationConfig {

    @Bean
    public ToolCallback[] registerTools() {
        return ToolCallbacks.from(
                new DateTimeTools(),
                new WeatherTools(),
                new FileOperationTool()
        );
        // 注册工具
    }
}
