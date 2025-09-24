package top.kimwonjoon.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName Avatar
 * @Description Avatar PO对象
 * @Author @kimwonjoon
 * @Date 2025/1/27
 */
@Data
public class Avatar {
    
    /**
     * 主键ID
     */
    private Integer id;
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 系统提示词
     */
    private String systemPrompt;
    /**
     * 模型ID
     */
    private Integer modelId;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}