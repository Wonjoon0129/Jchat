package top.kimwonjoon.domain.chat.service.voice.util;

import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisResult;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.*;

@Slf4j
public class AudioUtil {

    private static String apiKey="*";
    private static String model = "cosyvoice-v2"; // 模型

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

    /**
    * 文本转语音(流式) - 返回音频数据用于前端播放
    */
    public static Flux<byte[]> textToAudioStream(Flux<String> textStream, String voice,StringBuilder aiResponseBuilder) {

        return Flux.create(sink -> {
            List<byte[]> audioChunks = new ArrayList<>();

            // 配置回调函数
            ResultCallback<SpeechSynthesisResult> callback = new ResultCallback<SpeechSynthesisResult>() {

                @Override
                public void onEvent(SpeechSynthesisResult result) {
                    if (result.getAudioFrame() != null) {
                        byte[] audioData = result.getAudioFrame().array();

                        // 确保音频块大小合适，避免过小的块造成卡顿
                        audioChunks.add(audioData);

                        // 当累积的音频数据达到一定大小时再发送
                        int totalSize = audioChunks.stream().mapToInt(chunk -> chunk.length).sum();
                        if (totalSize >= 4096) { // 4KB 阈值
                            byte[] mergedChunk = mergeAudioChunks(audioChunks);
                            sink.next(mergedChunk);
                            audioChunks.clear();
                        }
                    }
                }

                @Override
                public void onComplete() {
                    log.info("收到Complete，语音合成结束");

                    // 发送剩余的音频数据
                    if (!audioChunks.isEmpty()) {
                        byte[] mergedChunk = mergeAudioChunks(audioChunks);
                        sink.next(mergedChunk);
                        audioChunks.clear();
                    }

                    sink.complete();
                }

                @Override
                public void onError(Exception e) {
                    log.info("出现异常：" + e.toString());
                    sink.error(e);
                }
            };

            // 请求参数
            SpeechSynthesisParam param =
                    SpeechSynthesisParam.builder()
                            .apiKey(apiKey)
                            .model(model)
                            .voice(!voice.isBlank()?voice:"longxiaochun_v2")
                            .format(SpeechSynthesisAudioFormat.MP3_48000HZ_MONO_256KBPS)
                            .build();
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, callback);

            try {
                textStream
                        .doOnNext(text -> {
                            try {
                                aiResponseBuilder.append(text);
                                synthesizer.streamingCall(text);
                            } catch (Exception e) {
                                sink.error(e);
                            }
                        })
                        .doOnComplete(() -> {
                            try {
                                synthesizer.streamingComplete();
                            } catch (Exception e) {
                                sink.error(e);
                            }
                        })
                        .doOnError(error -> {
                            synthesizer.getDuplexApi().close(1000, "error");
                            sink.error(error);
                        })
                        .doFinally(signalType -> {
                            synthesizer.getDuplexApi().close(1000, "bye");
                        })
                        .subscribe();

            } catch (Exception e) {
                log.info("处理文本流时出现异常: " + e.getMessage());
                sink.error(e);
            }
        });
    }

        /**
         * 合并音频块
         */
        private static byte[] mergeAudioChunks(List<byte[]> chunks) {
            int totalLength = chunks.stream().mapToInt(chunk -> chunk.length).sum();
            byte[] merged = new byte[totalLength];
            int offset = 0;

            for (byte[] chunk : chunks) {
                System.arraycopy(chunk, 0, merged, offset, chunk.length);
                offset += chunk.length;
            }
            return merged;
        }
    }