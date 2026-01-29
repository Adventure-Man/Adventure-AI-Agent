package com.adventure.adventureaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @author Adventure
 * @date 2026/4/2
 * @description 测试天气查询工具
 */
public class WeatherTools {
    @Tool(description = "查询当地的天气")
    public String getWeather( @ToolParam(description = "城市名称") String city) {
        return "天气查询功能暂未实现";
    }
}
