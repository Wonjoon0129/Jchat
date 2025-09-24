package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.Avatar;

/**
 * @ClassName IAvatarDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/24 09:38
 */
@Mapper
public interface IAvatarDao {

    /**
     * 根据id查询
     * @param avatarId
     */
    Avatar queryById(Integer avatarId);
}
