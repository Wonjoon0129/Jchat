package top.kimwonjoon.domain.chat.service.chat;

/**
 * @ClassName AiChatService
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/23 09:19
 */

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import top.kimwonjoon.domain.chat.service.armory.factory.DefaultArmoryStrategyFactory;

import java.time.LocalDate;

@Service
public class AiChatService {
    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";

    @Resource
    DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    /**
     * 生成AI回复（非流式）
     */
    public String generateResponse(String userMessage,Integer avatarId,String roomId) {
        ChatClient chatClient = defaultArmoryStrategyFactory.chatClient(avatarId);

        String chatId=avatarId+"_"+roomId;
        String content = chatClient.prompt(userMessage)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call().content();
        return content;

    }

    /**
     * 生成AI回复（流式）
     */
    public Flux<String> generateStreamResponse(String userMessage,Integer avatarId,String roomId) {
        ChatClient chatClient = defaultArmoryStrategyFactory.chatClient(avatarId);

        String chatId=avatarId+"_"+roomId;
        Flux<String> content = chatClient.prompt(userMessage)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream().content();
        return content;

    }
}