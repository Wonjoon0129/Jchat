package top.kimwonjoon.infrastructure.dao.po;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @ClassName Model
 * @Description Model PO对象
 * @Author @kimwonjoon
 * @Date 2025/1/27
 */
@Data
public class Model {
    
    /**
     * 主键ID
     */
    private Integer id;
    
    /**
     * 模型名称
     */
    private String modelName;
    
    /**
     * 基础URL
     */
    private String baseUrl;
    
    /**
     * API密钥
     */
    private String apiKey;
    
    /**
     * 完成路径
     */
    private String completionsPath;
    
    /**
     * 模型版本
     */
    private String modelVersion;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}