package com.adventure.adventureaiagent.apimodel;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DashscopeTextGeneration {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;


    public void callDashscopeText() {
        // ---------------------- 1. 配置核心参数 ----------------------
        // 接口地址
        String apiUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        // 你的DashScope API密钥，替换为实际值
        // 手动创建 Spring 上下文并获取 Bean
        String dashScopeApiKey = apiKey;


        // ---------------------- 2. 构建请求体（JSON格式） ----------------------
        // 方式1：使用Hutool的JSON工具类构建（推荐，更易维护）
        JSONObject requestBody = new JSONObject();
        // 设置模型
        requestBody.put("model", "qwen-plus");

        // 构建input部分：对话消息列表
        JSONArray messages = new JSONArray();
        // System角色消息
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", "You are a helpful assistant.");
        messages.add(systemMsg);
        // User角色消息
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", "你是谁？");
        messages.add(userMsg);
        requestBody.put("input", new JSONObject().put("messages", messages));

        // 构建parameters部分：返回格式配置
        JSONObject parameters = new JSONObject();
        parameters.put("result_format", "message");
        requestBody.put("parameters", parameters);

        // ---------------------- 3. 发送HTTP POST请求 ----------------------
        try (HttpResponse response = HttpRequest.post(apiUrl)
                // 设置Authorization头：Bearer + API密钥
                .header("Authorization", "Bearer " + dashScopeApiKey)
                // 设置内容类型为JSON
                .header("Content-Type", "application/json")
                // 设置请求体为JSON字符串
                .body(JSONUtil.toJsonStr(requestBody))
                // 执行请求
                .execute()) {

            // ---------------------- 4. 处理响应结果 ----------------------
            if (response.isOk()) {
                // 响应成功，格式化输出结果
                System.out.println("请求成功，响应结果：");
                System.out.println(JSONUtil.parseObj(response.body()).toStringPretty());
            } else {
                // 响应失败，输出状态码和错误信息
                System.err.println("请求失败，状态码：" + response.getStatus());
                System.err.println("错误信息：" + response.body());
            }
        } catch (Exception e) {
            // 捕获请求异常
            System.err.println("请求发生异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

}
