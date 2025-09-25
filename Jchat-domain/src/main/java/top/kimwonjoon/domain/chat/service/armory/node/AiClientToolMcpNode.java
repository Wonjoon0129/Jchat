package top.kimwonjoon.domain.chat.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.dashscope.utils.JsonUtils;
import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.chat.model.entity.TransportConfigStdio;
import top.kimwonjoon.domain.chat.model.valobj.enums.AiAgentEnumVO;
import top.kimwonjoon.domain.chat.service.armory.AbstractArmorySupport;
import top.kimwonjoon.domain.chat.service.armory.factory.DefaultArmoryStrategyFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @ClassName Ai
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 19. 15:00
 * @Version 1.0
 */
@Slf4j
@Component
public class AiClientToolMcpNode extends AbstractArmorySupport {

    @Resource
    private AiClientModelNode aiClientModelNode;


    @Override
    protected String doApply(Integer avatarId, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
            // 创建McpSyncClient对象
            McpSyncClient mcpSyncClient = createMcpSyncClient();
            // 使用父类的通用注册方法
            registerBean(beanName(1L), McpSyncClient.class, mcpSyncClient);
        return router(avatarId, dynamicContext);
    }

    protected McpSyncClient createMcpSyncClient() {
        String json="{\"stdio\": {\n" +
                "    \"gradio\": {\n" +
                "      \"args\": [\n" +
                "        \"mcp-remote\",\n" +
                "        \"https://phoenixdna-sogou-search.ms.show/gradio_api/mcp/sse\",\n" +
                "        \"--transport\",\n" +
                "        \"sse-only\"\n" +
                "      ],\n" +
                "      \"command\": \"npx\"\n" +
                "    }\n" +
                "}\n" +
                "}";
        TransportConfigStdio transportConfigStdio= JsonUtils.fromJson(json, TransportConfigStdio.class);
        Map<String, TransportConfigStdio.Stdio> stdioMap = transportConfigStdio.getStdio();
        TransportConfigStdio.Stdio stdio = stdioMap.get("gradio");
        // https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem
        var stdioParams = ServerParameters.builder(stdio.getCommand())
                .args(stdio.getArgs())
                .build();
        var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(180)).build();
        var init_stdio = mcpClient.initialize();
        log.info("Tool Stdio MCP Initialized {}", init_stdio);
        return mcpClient;
    }

    @Override
    protected String beanName(Long id) {
        return AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanNameTag() + id;
    }

    @Override
    public StrategyHandler<Integer,DefaultArmoryStrategyFactory.DynamicContext, String> get(Integer avatarId, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientModelNode;
    }
}
