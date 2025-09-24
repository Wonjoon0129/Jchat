package top.kimwonjoon.domain.chat.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.chat.model.valobj.AvatarVO;
import top.kimwonjoon.domain.chat.model.valobj.ModelVO;
import top.kimwonjoon.domain.chat.service.armory.AbstractArmorySupport;
import top.kimwonjoon.domain.chat.service.armory.factory.DefaultArmoryStrategyFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @ClassName RootNode
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 19. 12:12
 * @Version 1.0
 */
@Slf4j
@Component
public class RootNode extends AbstractArmorySupport {

    @Resource
    private AiClientModelNode aiClientModelNode;

    @Override
    protected void multiThread(Integer avatarId, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<ModelVO> aiClientModelFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(aiClientModel) {}",avatarId);
            return repository.queryModel(avatarId);
        }, threadPoolExecutor);

        CompletableFuture<AvatarVO> aiClientFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(avatar) {}",avatarId);
            return repository.queryAvatar(avatarId);
        }, threadPoolExecutor);


        CompletableFuture.allOf(aiClientModelFuture)
                .thenRun(() -> {
                    dynamicContext.setValue("aiClientModel", aiClientModelFuture.join());
                    dynamicContext.setValue("aiClient", aiClientFuture.join());
                }).join();

    }

    @Override
    protected String doApply(Integer avatarId, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，数据加载节点 {}", JSON.toJSONString(avatarId));
        return router(avatarId, dynamicContext);
    }

    @Override
    public StrategyHandler<Integer, DefaultArmoryStrategyFactory.DynamicContext, String> get(Integer avatarId, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientModelNode;
    }
}

