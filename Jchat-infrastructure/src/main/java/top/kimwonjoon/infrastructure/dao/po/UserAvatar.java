package top.kimwonjoon.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName UserAvatar
 * @Description UserAvatar PO对象
 * @Author @kimwonjoon
 * @Date 2025/1/27
 */
@Data
public class UserAvatar {
    
    /**
     * 主键ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 头像ID
     */
    private Integer avatarId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}