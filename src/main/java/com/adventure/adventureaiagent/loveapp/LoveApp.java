package com.adventure.adventureaiagent.loveapp;

import com.adventure.adventureaiagent.common.constant.FileConstant;
import com.adventure.adventureaiagent.rag.QueryRewriter;
import com.adventure.adventureaiagent.tools.WeatherTools;
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

//    @Autowired
//    @Qualifier("zhiPuAiChatModel")
//    private ChatModel zhiPuAiChatModel;

    @Resource
    private ToolCallback[] registerTools;

//    @Resource
//    private ToolCallbackProvider toolCallbackProvider;

    @Resource
    @Qualifier("fileBasedChatMemory")
    private ChatMemory fileBasedChatMemory;

//    @Autowired
//    private Advisor loveAppRagCloudAdvisor;

//    @Resource
//    private VectorStore loveAppVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 构造函数 构造一个基于dashScopeChatModel的模型
     */
    /*public LoveApp(@Qualifier("dashScopeChatModel")ChatModel dashScopeChatModel) {
        this.chatModel = zhiPuAiChatModel;
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一个恋爱情感专家,你能回答用户的各种情感问题")
                .build();
    }*/

    /**
     * 构造函数 构造一个基于智谱的模型会话记忆功能
     * @param dashScopeChatModel
     */

//    public LoveApp(@Qualifier("zhiPuAiChatModel")ChatModel dashScopeChatModel) {
//        this.chatClient = ChatClient.builder(dashScopeChatModel)
//                .defaultSystem("你是一个恋爱情感专家,你能回答用户的各种情感问题")
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(fileBasedChatMemory).build())
//                .build();
//    }
    // PostConstruct:解决构造器加载时依赖还没注入的问题
//    1.构造函数执行
//    2.依赖注入（@Autowired、@Resource 等）
//    3.@PostConstruct 方法执行 ← 这时所有依赖都已注入完成

    private ChatClient chatClient;

    @PostConstruct
    public void init() {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(FileConstant.SYSTEM_PROMPT_LOVE_APP)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(fileBasedChatMemory).build())
                .build();
    }

    /**
     * sse 流式输出
     * @param message
     * @return
     */
    public Flux<String> doChatWithModelSse(String message, String chatId) {
        Flux<String> content = chatClient.prompt()
                .user(message)
                .advisors(new SimpleLoggerAdvisor())
                // 多轮对话记忆 设置chatId参数，将参数传递给ChatMemory.add()
                .advisors(spec -> spec.param("conversationId", chatId)
                        // 基于内存对话记忆，设置记忆条数为10
                        .param(TOP_K, 10))
                .toolCallbacks(registerTools)
                .stream()
                .content();
        log.info("content: {}", content);
        return content;
    }


    /**
     * 聊天 1.查询重写 2.系统提示词变量传递
     * @param message
     * @return
     */
    public Flux<String> doChat(String message, String chatId) {
        String doQueryRewrite = queryRewriter.doQueryRewrite(message);
//        ChatClient chatClient = ChatClient.builder(chatModel)
//                .defaultSystem("你是一只狗,你只会回答{voice}" )
//                .build();
        Flux<String> content = chatClient.prompt()
                .system(a -> a.param("voice", "汪汪汪"))
                .user(doQueryRewrite)
                .stream()
                .content();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 聊天 1.查询重写 2.系统提示词变量传递
     * @param message
     * @return
     */
    public String doChatWithMemory(String message, String chatId) {
        record Weather(String city, String temperature) {
        }
        Weather content = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param("conversationId", chatId))
                .advisors(new SimpleLoggerAdvisor())
                .tools(new WeatherTools())
                .call()
                        .entity(Weather.class);
//                .content();
        log.info("content: {}", content);
        return content.toString();
    }


    /**
     * 构建rag知识库聊天
     * @param message
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 查询重写
        String doQueryRewrite = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(doQueryRewrite)
                .advisors(spec -> spec.param("conversationId", chatId)
                        .param(TOP_K, 10))
                // 开启日志，便于观察效果
                .advisors(new SimpleLoggerAdvisor())
                // 应用知识库问答v
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
//                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    /**
     * 工具调用
     * @param message
     * @return
     */
/*    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
//                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new SimpleLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }*/


}
