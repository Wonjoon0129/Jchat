package top.kimwonjoon.trigger.http;

/**
 * @ClassName WebSocketController
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/23 09:20
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import top.kimwonjoon.api.dto.VoiceMessage;
import top.kimwonjoon.domain.chat.service.chat.AiChatService;
import top.kimwonjoon.domain.chat.service.preheat.AiAgentPreheatService;
import top.kimwonjoon.domain.chat.service.voice.VoiceProcessingService;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Controller
public class VoiceChatController {

    @Resource
    private AiAgentPreheatService aiAgentPreheatService;

    @Resource
    private SimpMessagingTemplate messagingTemplate;
    @Resource
    private VoiceProcessingService voiceProcessingService;
    @Resource
    private AiChatService aiChatService;

    public VoiceChatController(SimpMessagingTemplate messagingTemplate,
                               VoiceProcessingService voiceProcessingService,
                               AiChatService aiChatService) {
        this.messagingTemplate = messagingTemplate;
        this.voiceProcessingService = voiceProcessingService;
        this.aiChatService = aiChatService;
    }

    /**
     * 处理语音消息
     */
    @MessageMapping("/voice")
    public void handleVoiceMessage(@Payload VoiceMessage message) {

        String username = message.getSender() != null ? message.getSender() : "user";
        message.setSender(username);

        log.info("收到来自用户 {} 的语音消息", username);

        // 异步处理语音消息
        CompletableFuture.runAsync(() -> {
            try {
                switch (message.getType()) {
                    case VOICE_DATA:
                        handleVoiceData(message);
                        break;
                    case TEXT_MESSAGE:
                        handleTextMessage(message);
                        break;
                    default:
                        log.warn("未知的消息类型: {}", message.getType());
                }
            } catch (Exception e) {
                log.error("处理语音消息时出错", e);
                sendErrorMessage(message.getSender(), "处理消息时出错: " + e.getMessage());
            }
        });
    }

    /**
     * 处理语音数据
     */
    private void handleVoiceData(VoiceMessage message) {

        // 转录语音为文本
        String transcription = voiceProcessingService.transcribeAudio(
                message.getAudioData()
        );
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(transcription);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String textContent = jsonNode.get("output")
                .get("choices")
                .get(0)
                .get("message")
                .get("content")
                .get(0)
                .get("text")
                .asText();
        log.info("语音转录结果: {}", textContent);
        message.setContent(textContent);
        handleTextMessage(message);
    }


    /**
     * 处理文本消息
     */
    private void handleTextMessage(VoiceMessage message) {

        Integer avatarId = Integer.valueOf(message.getAvatarId());
        String voice=aiAgentPreheatService.getAvatarVoiceName(avatarId);
        message.setType(VoiceMessage.MessageType.TEXT_MESSAGE);
        try {
            // 1. 广播文本消息
            messagingTemplate.convertAndSend(
                    "/topic/room/" + message.getRoomId(),
                    message
            );

            // 2. 生成AI回复
            Flux<String> aiResponse = aiChatService.generateStreamResponse(message.getContent(), Integer.valueOf(message.getAvatarId()),message.getRoomId() );
            StringBuilder aiResponseBuilder = new StringBuilder();
            // 3. 将AI回复转换为音频流
            Flux<byte[]> audioStream = voiceProcessingService.textToSpeechStream(aiResponse,voice,aiResponseBuilder);

            // 4. 发送音频流到前端
            audioStream
                    .doOnNext(audioChunk -> {
                        // 将音频数据编码为Base64发送到前端
                        String base64Audio = Base64.getEncoder().encodeToString(audioChunk);

                        VoiceMessage audioChunkMessage = new VoiceMessage(
                                VoiceMessage.MessageType.AI_AUDIO_CHUNK,
                                "AI Assistant",
                                null
                        );
                        audioChunkMessage.setAudioData(base64Audio);
                        audioChunkMessage.setAudioFormat("pcm");
                        audioChunkMessage.setRoomId(message.getRoomId());

                        messagingTemplate.convertAndSend(
                                "/topic/room/" + message.getRoomId(),
                                audioChunkMessage
                        );
                    })
                    .doOnComplete(() -> {
                        // 发送音频流结束信号
                        VoiceMessage endMessage = new VoiceMessage(
                                VoiceMessage.MessageType.AI_AUDIO_END,
                                "AI Assistant",
                                null
                        );
                        endMessage.setRoomId(message.getRoomId());

                        messagingTemplate.convertAndSend(
                                "/topic/room/" + message.getRoomId(),
                                endMessage
                        );

                        //发送文本到前端
                        VoiceMessage aiVoiceMessage = new VoiceMessage(
                                VoiceMessage.MessageType.AI_RESPONSE,
                                "AI Assistant",
                                aiResponseBuilder.toString()
                        );
                        aiVoiceMessage.setRoomId(message.getRoomId());

                        messagingTemplate.convertAndSend(
                                "/topic/room/" + message.getRoomId(),
                                aiVoiceMessage
                        );
                    })
                    .doOnError(error -> {
                        log.error("音频流处理出错", error);
                        sendErrorMessage(message.getSender(), "音频流处理失败: " + error.getMessage());
                    })
                    .subscribe();




        } catch (Exception e) {
            log.error("处理文本消息时出错", e);
            sendErrorMessage(message.getSender(), "处理文本消息失败: " + e.getMessage());
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(String recipient, String errorMessage) {
        VoiceMessage errorMsg = new VoiceMessage(
                VoiceMessage.MessageType.TEXT_MESSAGE,
                "System",
                errorMessage
        );

        messagingTemplate.convertAndSendToUser(
                recipient,
                "/queue/errors",
                errorMsg
        );
    }

    /**
     * 处理用户加入房间
     */
    @MessageMapping("/join")
    public void handleUserJoin(@Payload VoiceMessage message) {
        try {
            aiAgentPreheatService.preheat(Integer.valueOf(message.getAvatarId()));
        } catch (Exception e) {
            log.info("加载 avatar 失败");
            throw new RuntimeException(e);
        }
        String username = message.getSender() != null ? message.getSender() : "user";

        VoiceMessage joinMessage = new VoiceMessage(
                VoiceMessage.MessageType.USER_JOIN,
                username,
                username + " 加入了聊天室"
        );
        joinMessage.setRoomId(message.getRoomId());

        messagingTemplate.convertAndSend(
                "/topic/room/" + message.getRoomId(),
                joinMessage
        );

        log.info("用户 {} 加入房间 {}", username, message.getRoomId());
    }
}
