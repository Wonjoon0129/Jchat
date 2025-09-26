package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.Avatar;

import java.util.List;

/**
 * @ClassName IAvatarDao
 * @Description Avatar数据访问接口
 * @Author @kimwonjoon
 * @Date 2025/9/24 09:38
 */
@Mapper
public interface IAvatarDao {

    /**
     * 插入Avatar
     * @param avatar Avatar对象
     * @return 影响行数
     */
    int insert(Avatar avatar);

    /**
     * 更新Avatar
     * @param avatar Avatar对象
     * @return 影响行数
     */
    int update(Avatar avatar);

    /**
     * 根据id查询
     * @param avatarId Avatar ID
     * @return Avatar对象
     */
    Avatar queryById(Integer avatarId);

    /**
     * 根据分类查询
     * @param category 分类
     * @return Avatar列表
     */
    List<Avatar> queryByCategory(String category);

    /**
     * 查询所有Avatar
     * @return Avatar列表
     */
    List<Avatar> queryAll();

    /**
     * 根据ID删除Avatar
     * @param id Avatar ID
     * @return 影响行数
     */
    int deleteById(Integer id);
}
