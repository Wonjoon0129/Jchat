package top.kimwonjoon.domain.chat.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.dashscope.utils.JsonUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.chat.model.entity.TransportConfigStdio;
import top.kimwonjoon.domain.chat.model.valobj.AvatarVO;
import top.kimwonjoon.domain.chat.model.valobj.enums.AiAgentEnumVO;
import top.kimwonjoon.domain.chat.service.armory.AbstractArmorySupport;
import top.kimwonjoon.domain.chat.service.armory.factory.DefaultArmoryStrategyFactory;
import top.kimwonjoon.domain.chat.service.armory.factory.element.RagAnswerAdvisor;
import top.kimwonjoon.domain.chat.service.armory.factory.element.RagQueryTransformer;
import top.kimwonjoon.domain.chat.service.armory.factory.element.RedisChatMemory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiClientNode extends AbstractArmorySupport {

    private ObjectMapper objectMapper = new ObjectMapper();


    @Resource
    RedisChatMemory chatMemory;

    @Resource
    VectorStore vectorStore;

    @Override
    protected String doApply(Integer avatarId, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，对话模型节点 {}", JSON.toJSONString(avatarId));

        AvatarVO avatarVO = dynamicContext.getValue("aiClient");

        // 1. 预设话术
        String defaultSystem =avatarVO.getSystemPrompt();

        // 2. chatModel
        ChatModel chatModel = getBean(AiAgentEnumVO.AI_CLIENT_MODEL.getBeanNameTag()+avatarVO.getModelId());

        // 3. ToolCallbackProvider
        List<McpSyncClient> mcpSyncClients = new ArrayList<>();
        McpSyncClient mcpSyncClient = getBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanNameTag() + 1);
        mcpSyncClients.add(mcpSyncClient);


        // 4. Advisor
        List<Advisor> advisors = new ArrayList<>();
        //记忆
        PromptChatMemoryAdvisor memoryAdvisor = PromptChatMemoryAdvisor.builder(chatMemory)
                .order(2)
                .build();
        advisors.add(memoryAdvisor);
        //Rag
//        RagQueryTransformer ragQueryTransformer= new RagQueryTransformer("question_answer_context",chatModel);
//        RagAnswerAdvisor ragAnswerAdvisor = new RagAnswerAdvisor(vectorStore, SearchRequest.builder()
//                .topK(4)
//                .build(), ragQueryTransformer);
//        advisors.add(ragAnswerAdvisor);

        Advisor[] advisorArray = advisors.toArray(new Advisor[]{});

        // 5. 构建对话客户端
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(defaultSystem)
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients.toArray(new McpSyncClient[]{})))
                .defaultAdvisors(advisorArray)
                .build();

        registerBean(beanName(Long.valueOf(avatarVO.getId())), ChatClient.class, chatClient);
            log.info("Ai Agent 构建完成 {}", beanName(Long.valueOf(avatarVO.getId())));


        return router(avatarId, dynamicContext);

    }

    @Override
    public StrategyHandler<Integer, DefaultArmoryStrategyFactory.DynamicContext, String> get(Integer avatarId, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }

    @Override
    protected String beanName(Long id) {
        return AiAgentEnumVO.CHAT_CLIENT.getBeanNameTag() + id;
    }
}