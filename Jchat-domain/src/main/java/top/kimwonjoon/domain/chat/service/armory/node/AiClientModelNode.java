package top.kimwonjoon.domain.chat.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.Model;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.chat.model.valobj.ModelVO;
import top.kimwonjoon.domain.chat.model.valobj.enums.AiAgentEnumVO;
import top.kimwonjoon.domain.chat.service.armory.AbstractArmorySupport;
import top.kimwonjoon.domain.chat.service.armory.factory.DefaultArmoryStrategyFactory;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AiClientModelNode
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/21 09:04
 * @Version 1.0
 */
@Slf4j
@Component
public class AiClientModelNode extends AbstractArmorySupport {

    @Resource
    private AiClientNode aiClientNode;

    @Override
    protected String doApply(Integer avatarId, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，客户端构建节点 {}", JSON.toJSONString(avatarId));

        ModelVO modelVO = dynamicContext.getValue("aiClientModel");

        if (modelVO == null) {
            log.warn("没有可用的AI客户端模型配置");
            return null;
        }

        ChatModel chatModel =createOpenAiChatModel(modelVO);
        registerBean(beanName(Long.valueOf(modelVO.getId())), ChatModel.class, chatModel);

        return router(avatarId, dynamicContext);
    }

    @Override
    public StrategyHandler<Integer, DefaultArmoryStrategyFactory.DynamicContext, String> get(Integer avatarId, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientNode;
    }


    @Override
    protected String beanName(Long id) {
        return AiAgentEnumVO.AI_CLIENT_MODEL.getBeanNameTag() + id;
    }

    /**
     * 创建OpenAiChatModel对象
     *
     * @param modelVO 模型配置值对象
     * @return OpenAiChatModel实例
     */
    public ChatModel createOpenAiChatModel(ModelVO modelVO) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                        .baseUrl(modelVO.getBaseUrl())
                        .apiKey(modelVO.getApiKey())
                        .completionsPath(modelVO.getCompletionsPath())
                        .build();
        List<McpSyncClient> mcpSyncClients = new ArrayList<>();
        // 仅在需要时添加MCP客户端，暂时注释掉可能引起问题的代码
        /*
        HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport.builder("http://appbuilder.baidu.com/v2/ai_search/mcp").sseEndpoint("/sse?api_key=bce-v3/ALTAK-WrLJzo59tSiLjxyaPdwWp/2a1d2fd41f6f1cfa37a257fbc5be78a4d9513bd3").build();
        McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport).requestTimeout(Duration.ofMinutes(150)).build();
        mcpSyncClients.add(mcpSyncClient);
        */

        // 构建OpenAiChatModelz
        return OpenAiChatModel.builder()
                        .openAiApi(openAiApi)
                        .defaultOptions(OpenAiChatOptions.builder()
                                .model(modelVO.getModelVersion())
                                .toolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients).getToolCallbacks())
                                .build())
                        .build();
    }


}