package top.kimwonjoon.trigger.http;

/**
 * @ClassName WebSocketController
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/23 09:20
 */

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
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


    private final SimpMessagingTemplate messagingTemplate;
    private final VoiceProcessingService voiceProcessingService;
    private final AiChatService aiChatService;

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
        try {
            // 1. 转录语音为文本
            String transcription = voiceProcessingService.transcribeAudio(
                    message.getAudioData()
            );

            log.info("语音转录结果: {}", transcription);
            //显示语音输入
            VoiceMessage transcriptionMessage = new VoiceMessage(
                    VoiceMessage.MessageType.TRANSCRIPTION,
                    message.getSender(),
                    transcription
            );
            transcriptionMessage.setRoomId(message.getRoomId());

            messagingTemplate.convertAndSend(
                    "/topic/room/" + message.getRoomId(),
                    transcriptionMessage
            );

            // 2. 生成AI回复
            String aiResponse = aiChatService.generateResponse(transcription, Integer.valueOf(message.getAvatarId()),message.getRoomId() );

            // 3. 将AI回复转换为语音
            String audioResponse = voiceProcessingService.textToSpeech(aiResponse, "alloy");

            // 5. 发送AI语音回复
            VoiceMessage aiVoiceMessage = new VoiceMessage(
                    VoiceMessage.MessageType.AI_RESPONSE,
                    "AI Assistant",
                    aiResponse
            );
            aiVoiceMessage.setAudioData(audioResponse);
            aiVoiceMessage.setAudioFormat("mp3");
            aiVoiceMessage.setRoomId(message.getRoomId());

            messagingTemplate.convertAndSend(
                    "/topic/room/" + message.getRoomId(),
                    aiVoiceMessage
            );

        } catch (Exception e) {
            log.error("处理语音数据时出错", e);
            sendErrorMessage(message.getSender(), "处理语音数据失败: " + e.getMessage());
        }
    }

    /**
     * 处理文本消息
     */
    private void handleTextMessage(VoiceMessage message) {
        try {
            // 1. 广播文本消息
            messagingTemplate.convertAndSend(
                    "/topic/room/" + message.getRoomId(),
                    message
            );

            // 2. 生成AI回复
            String aiResponse = aiChatService.generateResponse(message.getContent(), Integer.valueOf(message.getAvatarId()),message.getRoomId() );

            // 3. 将AI回复转换为语音
            String audioResponse = voiceProcessingService.textToSpeech(aiResponse, "alloy");


            // 4. 发送AI语音回复
            VoiceMessage aiVoiceMessage = new VoiceMessage(
                    VoiceMessage.MessageType.AI_RESPONSE,
                    "AI Assistant",
                    aiResponse
            );
            aiVoiceMessage.setAudioData(audioResponse);
            aiVoiceMessage.setAudioFormat("mp3");
            aiVoiceMessage.setRoomId(message.getRoomId());

            messagingTemplate.convertAndSend(
                    "/topic/room/" + message.getRoomId(),
                    aiVoiceMessage
            );

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
