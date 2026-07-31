package com.adventure.adventureaiagent.loveapp;

import com.adventure.adventureaiagent.common.constant.FileConstant;
import com.adventure.adventureaiagent.loveapp.advise.SimpleLoggerAdvisor;
import com.adventure.adventureaiagent.rag.QueryRewriter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor.TOP_K;


/**
 * @author Adventure
 * @date 2026/1/30
 * @description 恋爱大师应用
 */
@Component
@Slf4j
@SuppressWarnings("all")
public class LoveApp {
    @Autowired
    private ChatModel chatModel;

    @Resource
    private ToolCallback[] registerTools;

//    @Resource()
//    private ToolCallbackProvider toolCallbackProvider;

    @Resource
    @Qualifier("fileBasedChatMemory")
    private ChatMemory fileBasedChatMemory;

    //@Autowired
    //private Advisor loveAppRagCloudAdvisor;

    @Resource
    private VectorStore vectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    /*
    PostConstruct:解决构造器加载时依赖还没注入的问题
    1.构造函数执行
    2.依赖注入（@Autowired、@Resource 等）
    3.@PostConstruct 方法执行 ← 这时所有依赖都已注入完成
    */

    private ChatClient chatClient;

    @PostConstruct
    public void init() {
        this.chatClient = ChatClient.builder(chatModel).defaultSystem(FileConstant.SYSTEM_PROMPT_LOVE_APP).defaultAdvisors(MessageChatMemoryAdvisor.builder(fileBasedChatMemory).build()).build();
    }

    record Weather(String city, String temperature) {
    }

    /**
     * sse 流式输出
     *
     * @param message
     * @return
     */
    public Flux<String> doChatWithModelSse(String message, String chatId) {
        // 1.1查询重写
        // String doQueryRewrite = queryRewriter.doQueryRewrite(message);
        Flux<String> content = chatClient.prompt()
                // 1.2 追加系统提示词
                .system(FileConstant.SYSTEM_PROMPT_LOVE_APP)
                .user(message)
                // 1.3 advisors链 添加日志
                .advisors(new SimpleLoggerAdvisor())
                //.advisors(new MySimpleLoggerAdvisor())
                // 2 Memory 多轮对话记忆 设置chatId参数，会话隔离  将参数传递给ChatMemory.add()
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                        // 2.1 传递参数 设置记忆条数为10
                        .param(TOP_K, 20))
                // 3. Function Calling工具注册
                .toolCallbacks(registerTools)
                //.toolCallbacks(registerTools)
                // 4. RAG 知识库
                // 4.1 添加RAG 基于PgVectorStore 本地向量存储知识库
                //.advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                // 4.2 添加RAG 基于CloudVectorStore 跨云向量存储知识库
                //.advisors(loveAppRagCloudAdvisor)
                // 5. MCP
                // 6. 流式输出 结构化输出(配置Json Schamer)
                // 6.1 结构化输出
                //.entity(Weather.class);
                .stream().content();
        // 6.2 聊天响应
        //.chatResponse();
        log.info("content: {}", content);
        return content;
    }


    /**
     * MCP工具调用
     *
     * @param message
     * @return
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                        .param(TOP_K, 10))
                // 开启日志，便于观察效果
                .advisors(new SimpleLoggerAdvisor())
                //.tools(toolCallbackProvider)
                //.toolCallbacks(toolCallbackProvider)
                .call().chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


}
