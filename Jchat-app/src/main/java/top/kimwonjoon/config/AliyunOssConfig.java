package top.kimwonjoon.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.kimwonjoon.config.properties.AliyunOssConfigProperties;


/**
 * @ClassName AliyunOssConfig
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/22 20:32
 */
@Configuration
@EnableConfigurationProperties(AliyunOssConfigProperties.class)
public class AliyunOssConfig {

    @Bean("ossClient")
    public OSS ossClient(AliyunOssConfigProperties properties){

        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(properties.getAccessKeyId(), properties.getAccessKeySecret());

        return new OSSClientBuilder().build(properties.getEndpoint(), credentialsProvider);
    }

}
