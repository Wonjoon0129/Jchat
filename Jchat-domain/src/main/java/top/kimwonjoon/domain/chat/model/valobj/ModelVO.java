package top.kimwonjoon.domain.chat.model.valobj;

import lombok.Data;

/**
 * @ClassName ModelVO
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/24 10:00
 */
@Data
public class ModelVO {

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
}
