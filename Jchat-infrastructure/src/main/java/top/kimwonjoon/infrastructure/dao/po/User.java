package top.kimwonjoon.infrastructure.dao.po;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @ClassName User
 * @Description User PO对象
 * @Author @kimwonjoon
 * @Date 2025/1/27
 */
@Data
public class User {
    
    /**
     * 主键ID
     */
    private Integer id;
    
    /**
     * 用户名称
     */
    private Integer name;
    
    /**
     * 用户UUID
     */
    private String uuid;
    
    /**
     * 性别：1-男生，2-女生，0-未设置
     */
    private Integer sex;
    
    /**
     * 年龄
     */
    private Integer old;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}