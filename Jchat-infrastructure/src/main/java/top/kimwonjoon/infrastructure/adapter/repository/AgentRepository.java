package top.kimwonjoon.infrastructure.adapter.repository;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import top.kimwonjoon.domain.chat.adapter.repository.IAgentRepository;
import top.kimwonjoon.domain.chat.model.valobj.AvatarVO;
import top.kimwonjoon.domain.chat.model.valobj.ModelVO;
import top.kimwonjoon.infrastructure.dao.IAvatarDao;
import top.kimwonjoon.infrastructure.dao.IModelDao;
import top.kimwonjoon.infrastructure.dao.po.Avatar;
import top.kimwonjoon.infrastructure.dao.po.Model;

/**
 * @ClassName AgentRepository
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/24 09:35
 */
@Repository
public class AgentRepository implements IAgentRepository {
    @Resource
    private IAvatarDao avatarDao;

    @Resource
    private IModelDao modelDao;

    @Override
    public AvatarVO queryAvatar(Integer avatarId) {
        Avatar avatar=avatarDao.queryById(avatarId);
        AvatarVO avatarVO=new AvatarVO();
        avatarVO.setModelId(avatar.getModelId());
        avatarVO.setId(avatar.getId());
        avatarVO.setName(avatar.getName());
        avatarVO.setDescription(avatar.getDescription());
        avatarVO.setCategory(avatar.getCategory());
        avatarVO.setSystemPrompt(avatar.getSystemPrompt());

        return avatarVO;
    }

    @Override
    public ModelVO queryModel(Integer avatarId) {
        Model model=modelDao.queryById(avatarId);
        ModelVO modelVO=new ModelVO();
        modelVO.setId(model.getId());
        modelVO.setModelName(model.getModelName());
        modelVO.setBaseUrl(model.getBaseUrl());
        modelVO.setModelVersion(model.getModelVersion());
        modelVO.setApiKey(model.getApiKey());
        modelVO.setCompletionsPath(model.getCompletionsPath());
        return modelVO;

    }
}
