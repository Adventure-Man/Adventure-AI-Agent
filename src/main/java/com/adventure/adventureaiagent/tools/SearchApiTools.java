package com.adventure.adventureaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Adventure
 * @date 2026/4/2
 * @description 测试天气查询工具
 */
public class SearchApiTools {

    private final String apiKey;

    public SearchApiTools(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "联网搜索资料，用于查询学习路线、常识问答、推荐与总结等。多数情况下搜索后即可直接回答，无需再调用其他工具。")
    public String getSearchResult( @ToolParam(description = "search query key") String query) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("engine", "baidu");
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        try {
            String result = HttpUtil.get("https://www.searchapi.io/api/v1/search", paramMap);
            JSONObject entries = JSONUtil.parseObj(result);
            JSONArray organicResults = entries.getJSONArray("organic_results");
            // 获取JSONArray的前五条数据
            List<Object> firstFiveResults = organicResults.subList(0, 5);
            // 将前五条拼接为字符串 以“,”分隔
            StringBuilder sb = new StringBuilder();
            for (Object resultItem : firstFiveResults) {
                JSONObject resultItemJson = (JSONObject) resultItem;
                sb.append(resultItemJson.getStr("title")).append("\n");
                sb.append(resultItemJson.getStr("snippet")).append("\n");
                sb.append(resultItemJson.getStr("link")).append("\n");
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "error search by baidu:" + e.getMessage();
        }
    }
}
