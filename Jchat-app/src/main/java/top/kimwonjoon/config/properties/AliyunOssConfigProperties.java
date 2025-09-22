package top.kimwonjoon.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author kimwonjoon
 * @description 阿里云OSS配置属性
 * @create 2025-01-27
 */
@Data
@ConfigurationProperties(prefix = "aliyun.oss.config", ignoreInvalidFields = true)
public class AliyunOssConfigProperties {

    /** OSS服务端点 */
    private String endpoint = "oss-cn-beijing.aliyuncs.com";
    
    /** 访问密钥ID */
    private String accessKeyId;
    
    /** 访问密钥Secret */
    private String accessKeySecret;
    
    /** 默认存储桶名称 */
    private String bucketName = "java-hello-world";

}