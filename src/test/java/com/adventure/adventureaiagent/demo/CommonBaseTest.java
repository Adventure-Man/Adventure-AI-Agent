package com.adventure.adventureaiagent.demo;

import com.adventure.adventureaiagent.tools.WeatherTools;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.qcloud.cos.model.ExpressionType.SQL;

/**
 * @author Adventure
 * @date 2026/3/10
 * @description TODO
 */
@SpringBootTest
public class CommonBaseTest {
    @Autowired
    private ChatModel chatModel;

    @Test
    public void commonTest() {
// 先得到工具对象
        ToolCallback[] weatherTools = ToolCallbacks.from(new WeatherTools());
// 绑定工具到对话
        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(weatherTools)
                .build();
// 构造 Prompt 时指定对话选项
        Prompt prompt = new Prompt("北京今天天气怎么样？", chatOptions);
        String text = chatModel.call(prompt).getResult().getOutput().getText();

        System.out.println(text);
    }
}
