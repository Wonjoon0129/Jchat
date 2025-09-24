package top.kimwonjoon.domain.chat.service.preheat;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.Model;
import org.springframework.stereotype.Service;
import top.kimwonjoon.domain.chat.adapter.repository.IAgentRepository;
import top.kimwonjoon.domain.chat.service.armory.factory.DefaultArmoryStrategyFactory;
import top.kimwonjoon.domain.chat.service.armory.node.AiClientModelNode;

import java.util.List;

/**
 * 装配服务
 */
@Slf4j
@Service
public class AiAgentPreheatService {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;
    @Resource
    private IAgentRepository repository;
    @Resource
    AiClientModelNode aiClientModelNode;

    public void preheat(Integer avatarId) throws Exception {
        StrategyHandler<Integer, DefaultArmoryStrategyFactory.DynamicContext, String> handler = defaultArmoryStrategyFactory.strategyHandler();
        handler.apply(avatarId, new DefaultArmoryStrategyFactory.DynamicContext());
    }

}
