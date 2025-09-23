package top.kimwonjoon.trigger.http;

/**
 * @ClassName WebSocketController
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/23 09:20
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import top.kimwonjoon.api.dto.VoiceMessage;
import top.kimwonjoon.domain.chat.service.chat.AiChatService;
import top.kimwonjoon.domain.chat.service.audio.VoiceProcessingService;

import java.security.Principal;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Controller
public class VoiceChatController {


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
    public void handleVoiceMessage(@Payload VoiceMessage message,
                                   SimpMessageHeaderAccessor headerAccessor,
                                   Principal principal) {

        String username = principal != null ? principal.getName() : "Anonymous";
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
                    message.getAudioData(),
                    message.getAudioFormat()
            );

            log.info("语音转录结果: {}", transcription);

            // 2. 显示语音输入
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

            // 3. 生成AI回复
            String aiResponse ="测试";
//                    aiChatService.generateResponse(transcription, "");

            log.info("AI回复: {}", aiResponse);

            // 4. 将AI回复转换为语音
            byte[] audioResponse=new byte[]{1,2,3,4,5,6,7,8,9,10};
//                    = voiceProcessingService.textToSpeech(aiResponse, "alloy");
            String base64Audio = Base64.getEncoder().encodeToString(audioResponse);

            // 5. 发送AI语音回复
            VoiceMessage aiVoiceMessage = new VoiceMessage(
                    VoiceMessage.MessageType.AI_RESPONSE,
                    "AI Assistant",
                    aiResponse
            );
            aiVoiceMessage.setAudioData(base64Audio);
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
            String aiResponse = aiChatService.generateResponse(message.getContent(), "");

            // 3. 将AI回复转换为语音
            byte[] audioResponse = voiceProcessingService.textToSpeech(aiResponse, "alloy");
            String base64Audio = Base64.getEncoder().encodeToString(audioResponse);

            // 4. 发送AI语音回复
            VoiceMessage aiVoiceMessage = new VoiceMessage(
                    VoiceMessage.MessageType.AI_RESPONSE,
                    "AI Assistant",
                    aiResponse
            );
            aiVoiceMessage.setAudioData(base64Audio);
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
    public void handleUserJoin(@Payload VoiceMessage message, Principal principal) {
        String username = principal != null ? principal.getName() : "Anonymous";

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
