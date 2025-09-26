package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.User;

import java.util.List;

/**
 * @ClassName IUserDao
 * @Description 用户数据访问接口
 * @Author @kimwonjoon
 * @Date 2025/1/27
 */
@Mapper
public interface IUserDao {

    /**
     * 插入用户
     * @param user 用户对象
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 更新用户
     * @param user 用户对象
     * @return 影响行数
     */
    int update(User user);

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    User queryById(Integer id);

    /**
     * 根据UUID查询用户
     * @param uuid 用户UUID
     * @return 用户对象
     */
    User queryByUuid(String uuid);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> queryAll();

    /**
     * 根据ID删除用户
     * @param id 用户ID
     * @return 影响行数
     */
    int deleteById(Integer id);
}