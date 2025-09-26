package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.Model;

import java.util.List;

/**
 * @ClassName IModelDao
 * @Description Model数据访问接口
 * @Author @kimwonjoon
 * @Date 2025/9/24 10:03
 */
@Mapper
public interface IModelDao {

    /**
     * 插入Model
     * @param model Model对象
     * @return 影响行数
     */
    int insert(Model model);

    /**
     * 更新Model
     * @param model Model对象
     * @return 影响行数
     */
    int update(Model model);

    /**
     * 根据ID查询Model
     * @param id Model ID
     * @return Model对象
     */
    Model queryById(Integer id);

    /**
     * 根据模型名称查询
     * @param modelName 模型名称
     * @return Model对象
     */
    Model queryByModelName(String modelName);

    /**
     * 查询所有Model
     * @return Model列表
     */
    List<Model> queryAll();

    /**
     * 根据ID删除Model
     * @param id Model ID
     * @return 影响行数
     */
    int deleteById(Integer id);
}
