package top.kimwonjoon.domain.chat.adapter.repository;

import top.kimwonjoon.domain.chat.model.valobj.AvatarVO;
import top.kimwonjoon.domain.chat.model.valobj.ModelVO;

import java.util.List;

/**
 *@ClassName IAgentRepository
 *@Description  
 *@Author @kimwonjoon
 *@Date 2025/9/24 09:22
 */

public  interface IAgentRepository {
    AvatarVO queryAvatar(Integer avatarId);

    ModelVO queryModel(Integer avatarId);
}
