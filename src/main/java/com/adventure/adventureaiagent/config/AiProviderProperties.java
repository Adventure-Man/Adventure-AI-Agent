package com.adventure.adventureaiagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.ai")
public class AiProviderProperties {

    /**
     * 当前启用的模型提供商：
     * zhipu / dashscope
     */
    private String provider;
}