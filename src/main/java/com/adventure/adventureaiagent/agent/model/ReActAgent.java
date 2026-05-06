package com.adventure.adventureaiagent.agent.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类  
 * 实现了思考-行动的循环模式  
 */  
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent {  

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    @Override
    public String step() {
        try {
            boolean think = think();
            if (!think) {
                return "No action needed.";
            }
            return act();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred during action execution." + e.getMessage();
        }
    }
}
