package top.kimwonjoon.trigger.http;

/**
 * @ClassName WebSocketController
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/23 09:20
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import top.kimwonjoon.api.dto.VoiceMessage;
import top.kimwonjoon.domain.chat.service.chat.AiChatService;
import top.kimwonjoon.domain.chat.service.audio.VoiceProcessingService;

import java.security.Principal;
import java.util.Base64;
import java.util.List;
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


            //测试
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                    .apiKey("sk-854c7cdf301f41d79d3eaef3bf23cef0")
                    .completionsPath("/chat/completions")
                    .embeddingsPath("/embeddings")
                    .build();

            // 初始化 ChatModel
            ChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("qwen-plus")
                            .build())
                    .build();
            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultAdvisors() // 添加默认advisor以避免"No StreamAdvisors available to execute"错误
                    .build();

            String text = chatClient.prompt(Prompt.builder()
                    .messages(new UserMessage(transcription))
                    .build()).call().content();
            log.info("AI回复: {}", text);
            // 先缓存流，以便可以多次使用




            // 2. 生成AI回复
            String aiResponse =text;

            // 3. 将AI回复转换为语音
            String audioResponse = voiceProcessingService.textToSpeech(text, "alloy");

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
            String aiResponse = aiChatService.generateResponse(message.getContent(), "");

            // 3. 将AI回复转换为语音
            byte[] audioResponse = new byte[0]; // TODO: 实现文本转语音功能
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
