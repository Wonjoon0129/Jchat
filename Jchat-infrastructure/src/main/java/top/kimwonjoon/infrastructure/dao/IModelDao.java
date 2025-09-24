package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.Model;

/**
 * @ClassName IModelDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/24 10:03
 */
@Mapper
public interface IModelDao {
    Model queryById(Integer avatarId);
}
