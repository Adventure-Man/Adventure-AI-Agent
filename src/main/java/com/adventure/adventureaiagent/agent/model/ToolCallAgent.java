package com.adventure.adventureaiagent.agent.model;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    private final ToolCallback[] availableTools;

    private ChatResponse toolCallChatResponse;

    private final ToolCallingManager toolCallingManager;

    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }

    @Override
    public boolean think() {
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info(getName() + "的思考: " + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");

            List<AssistantMessage.ToolCall> visibleToolCalls = toolCallList.stream()
                    .filter(toolCall -> !isTerminateTool(toolCall.name()))
                    .toList();
            String toolCallInfo = visibleToolCalls.stream()
                    .map(toolCall -> String.format("工具：%s，参数：%s",
                            toolCall.name(),
                            toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);

            if (StringUtils.hasText(result)) {
                setLastThinkMessage(result);
            } else if (!visibleToolCalls.isEmpty()) {
                setLastThinkMessage("准备调用工具：\n" + toolCallInfo);
            } else if (!toolCallList.isEmpty()) {
                setLastThinkMessage("任务已完成，正在结束对话。");
            } else {
                setLastThinkMessage(null);
            }

            if (toolCallList.isEmpty()) {
                getMessageList().add(assistantMessage);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            String errorMessage = "处理时遇到错误: " + e.getMessage();
            setLastThinkMessage(errorMessage);
            getMessageList().add(new AssistantMessage(errorMessage));
            return false;
        }
    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "";
        }
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());

        boolean hasTerminate = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> isTerminateTool(response.name()));
        boolean hasOtherTools = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> !isTerminateTool(response.name()));

        if (hasTerminate && !hasOtherTools) {
            log.info("终止工具调用（不对用户展示）");
            setState(AgentState.FINISHED);
            return "";
        }
        if (hasTerminate) {
            log.info("同轮还有其他业务工具，暂不结束，等待下一轮总结");
        }

        String results = toolResponseMessage.getResponses().stream()
                .filter(response -> !isTerminateTool(response.name()))
                .map(this::formatToolResult)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }

    private String formatToolResult(ToolResponseMessage.ToolResponse response) {
        String toolName = response.name();
        String data = String.valueOf(response.responseData());
        return switch (toolName) {
            case "getWeather" -> "已查询天气：" + data;
            case "searchWeb", "getSearchResult" -> "已搜索网络：" + data;
            case "getCurrentDateTime" -> "当前日期：" + data;
            case "scrapeWebPage" -> formatScrapePreview(data);
            case "downloadResource" -> "已下载资源：" + data;
            default -> "工具 " + toolName + " 执行完成：" + data;
        };
    }

    private String formatScrapePreview(String data) {
        String preview = data.length() > 200 ? data.substring(0, 200) + "…" : data;
        return String.format("已抓取网页正文（约 %d 字）：%s", data.length(), preview);
    }
}
