package com.adventure.adventureaiagent.agent.model;

import com.adventure.adventureaiagent.loveapp.advise.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * Adventure智能体
 */
@Component
public class AdventureManus extends ToolCallAgent {

    public AdventureManus(ToolCallback[] allTools, ChatModel chatModel) {
        super(allTools);
        this.setName("AdventureManus");
        String SYSTEM_PROMPT = """
                你是 AdventureManus，一位全能 AI 助手，致力于解决用户提出的各类任务。
                你可以调用多种工具来高效完成复杂请求。
                
                重要规则：
                1. 必须使用简体中文回复用户，包括思考过程和最终回答。
                2. 先简要说明你的思考，再决定是否调用工具。
                3. 工具执行后，用中文总结结果并给出下一步计划或最终结论。
                4. 只有在已经用中文给出完整最终回答后，才能单独调用 doTerminate 结束对话。
                5. doTerminate 是内部结束工具，不要在回复中提及它。
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                请根据用户需求，主动选择最合适的工具或工具组合。
                对于复杂任务，可以分步骤使用不同工具逐步解决。
                每次使用工具后，请用中文清晰解释执行结果，并说明下一步计划。
                当你已经用中文给出完整最终回答后，再单独调用 doTerminate 结束对话，不要与其他工具同轮调用。
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxStep(20);
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
