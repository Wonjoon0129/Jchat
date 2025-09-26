package top.kimwonjoon.domain.chat.model.valobj;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName AvatarVO
 * @Description AvatarVO 对象
 * @Author @kimwonjoon
 * @Date 2025/1/27
 */
@Data
public class AvatarVO {
    
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
     * 模型ID
     */
    private Integer modelId;
    /**
     * 音色
     */
    private String voice;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 系统提示词
     */
    private String systemPrompt;

}