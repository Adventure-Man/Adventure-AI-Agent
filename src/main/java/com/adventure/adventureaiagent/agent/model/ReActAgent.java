package com.adventure.adventureaiagent.agent.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    private static final String TERMINATE_TOOL = "doTerminate";

    /**
     * 本轮 think 阶段的 LLM 文本结论，供推送给前端。
     */
    private String lastThinkMessage;

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果（不含终止工具）
     */
    public abstract String act();

    @Override
    public List<String> step() {
        List<String> outputs = new ArrayList<>();
        try {
            boolean needAct = think();
            if (StringUtils.hasText(lastThinkMessage)) {
                outputs.add(formatThink(getCurrentStep(), lastThinkMessage));
            }
            if (!needAct) {
                setState(AgentState.FINISHED);
                if (StringUtils.hasText(lastThinkMessage)) {
                    outputs.add(formatAnswer(lastThinkMessage));
                } else {
                    outputs.add(formatAnswer("本轮无需调用工具"));
                }
                return outputs;
            }
            String actResult = act();
            if (StringUtils.hasText(actResult)) {
                outputs.add(formatAction(actResult));
            }
            // 仅调用终止工具时 act 为空，但思考里通常已有最终回答
            if (getState() == AgentState.FINISHED && StringUtils.hasText(lastThinkMessage)) {
                outputs.add(formatAnswer(lastThinkMessage));
            }
            return outputs;
        } catch (Exception e) {
            log.error("步骤执行异常", e);
            outputs.add(formatAnswer("执行过程中发生错误：" + e.getMessage()));
            return outputs;
        }
    }

    protected boolean isTerminateTool(String toolName) {
        return TERMINATE_TOOL.equals(toolName);
    }

    private String formatThink(int stepNumber, String content) {
        return "【思考 " + stepNumber + "】\n" + content;
    }

    private String formatAction(String content) {
        return "【工具执行】\n" + content;
    }

    private String formatAnswer(String content) {
        return "【回答】\n" + content;
    }
}
