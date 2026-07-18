package com.adventure.adventureaiagent.config;

import com.adventure.adventureaiagent.tools.*;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Adventure
 * @date 2026/4/2
 * @description 工具注册配置
 */
@Configuration
public class ToolRegistrationConfig {
    @Value("${search.api.key}")
    String apiKey;

    @Value("${pdf.font.path}")
    private String fontPath;

    @Value("${pdf.font.fallback}")
    private String fallbackFont;

    @Value("${pdf.font.encoding}")
    private String fontEncoding;


    @Bean
    public ToolCallback[] registerTools() {
        return ToolCallbacks.from(
                new DateTimeTools(),
                new WeatherTools(),
                new FileOperationTool(),
                new SearchApiTools(apiKey),
                new PDFGenerationTool(fontPath, fallbackFont, fontEncoding),
                new ResourceDownloadTool(),
                new WebScrapingTool(),
                new TerminateTool()
        );
        // 注册工具
    }
}
