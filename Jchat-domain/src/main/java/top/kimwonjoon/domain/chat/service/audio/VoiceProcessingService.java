package top.kimwonjoon.domain.chat.service.audio;

/**
 * @ClassName VoiceProcessingService
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/23 09:17
 */

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import top.kimwonjoon.domain.chat.service.audio.util.AudioFormatUtil;
import top.kimwonjoon.domain.chat.service.audio.util.AudioUtil;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class VoiceProcessingService {

    @Resource
    private OSS ossClient;

    /**
     * 将音频数据转换为文本
     */
    public String transcribeAudio(String base64AudioData, String audioFormat) {
        byte[] audioBytes;
        // 如果不是WAV格式，先转换为WAV
        if (!"wav".equalsIgnoreCase(audioFormat)) {
            audioBytes = AudioFormatUtil.convertAudioFormat(base64AudioData, audioFormat, "webm");
        } else {
            audioBytes = Base64.getDecoder().decode(base64AudioData);
        }
        String url = "";
        String objectName =UUID.randomUUID().toString();
        //上传语音到 oss
        try {
            PutObjectResult putObjectResult = ossClient.putObject("audio-kimwonjoon", objectName,new ByteArrayInputStream(audioBytes));
            url="https://audio-kimwonjoon.oss-cn-beijing.aliyuncs.com/"+objectName;
            log.info("putObjectResult = " + putObjectResult.toString());
            log.info("url = " + url);
        }catch (Exception e){
            log.info("Error Message:" + e.getMessage());
        }

        String ans="";
        try {
            ans = AudioUtil.audioToText(url);
            log.info("s = " + ans);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ans;
    }

    /**
     * 将文本转换为语音（流式）
     */
    public Flux<byte[]> textToSpeechStream(String text, String voice) {
        return null;

    }

    /**
     * 将文本转换为语音（非流式）
     */
    public byte[] textToSpeech(String text, String voice) {
        return null;

    }
}
