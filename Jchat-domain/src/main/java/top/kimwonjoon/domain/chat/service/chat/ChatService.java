package top.kimwonjoon.domain.chat.service.chat;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import top.kimwonjoon.domain.chat.service.armory.factory.element.AudioUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.UUID;

/**
 * @ClassName ChatService
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/22 20:44
 */
@Slf4j
@Service
public class ChatService {

    @Resource
    private OSS ossClient;

    public String chat(String userId, String conversationId, String avatarId, MultipartFile file)
    {
        AudioUtil audioUtil = new AudioUtil();
        String url = "";
        String objectName = userId+conversationId+avatarId+UUID.randomUUID()+"_"+file.getName();
        //上传语音到 oss
        try {
            PutObjectResult putObjectResult = ossClient.putObject("audio-kimwonjoon", objectName,new ByteArrayInputStream(file.getBytes()));
            url="https://audio-kimwonjoon.oss-cn-beijing.aliyuncs.com/"+objectName;
            log.info("putObjectResult = " + putObjectResult.toString());
            log.info("url = " + url);
        }catch (Exception e){
            log.info("Error Message:" + e.getMessage());
        }
        try {
            String ans = audioUtil.audioToText(url);
            log.info("s = " + ans);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
