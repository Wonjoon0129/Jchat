package top.kimwonjoon.domain.chat.service.chat;

/**
 * @ClassName AiChatService
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/23 09:19
 */

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个友好的AI助手，专门用于语音聊天。请用简洁、自然的语言回复用户。")
                .build();
    }

    /**
     * 生成AI回复（流式）
     */
    public Flux<String> generateResponseStream(String userMessage, String conversationHistory) {
        return chatClient.prompt()
                .user(userMessage)
                .options(OpenAiChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_O.getValue())
                        .temperature(0.7)
                        .maxTokens(150)
                        .build())
                .stream()
                .content();
    }

    /**
     * 生成AI回复（非流式）
     */
    public String generateResponse(String userMessage, String conversationHistory) {
        return chatClient.prompt()
                .user(userMessage)
                .options(OpenAiChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_O.getValue())
                        .temperature(0.7)
                        .maxTokens(150)
                        .build())
                .call()
                .content();
    }
}