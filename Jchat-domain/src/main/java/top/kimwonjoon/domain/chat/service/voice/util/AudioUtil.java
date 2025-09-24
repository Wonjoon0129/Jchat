package top.kimwonjoon.domain.chat.service.voice.util;

import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.drew.metadata.mp3.Mp3Reader;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class AudioUtil {

    static Mp3Reader reader = new Mp3Reader();
    private static String apiKey="sk-854c7cdf301f41d79d3eaef3bf23cef0";
    private static String model = "qwen3-tts-flash"; // 模型
    private static String voice = "longxiaochun_v2"; // 音色


    private static String[] textArray = {"流式文本语音合成SDK，"};

    public static String audioToText(String url)
            throws Exception {

        // 使用 DashScope 进行音频转文本
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("audio",url)))
                .build();

        MultiModalMessage sysMessage = MultiModalMessage.builder()
                .role(Role.SYSTEM.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", "")))
                .build();

        Map<String, Object> asrOptions = new HashMap<>();
        asrOptions.put("enable_lid", true);
        asrOptions.put("enable_itn", false);

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model("qwen3-asr-flash")
                .message(userMessage)
                .message(sysMessage)
                .parameter("asr_options", asrOptions)
                .build();

        MultiModalConversationResult result = conv.call(param);
        return JsonUtils.toJson(result);
    }



    public static String textToAudio(String text, String voice) {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .text(text)
                .voice(AudioParameters.Voice.CHERRY)
                .build();
        MultiModalConversationResult result = null;
        try {
            result = conv.call(param);
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        }
        return result.getOutput().getAudio().getUrl();
    }

}