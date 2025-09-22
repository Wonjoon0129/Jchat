package top.kimwonjoon.trigger.http;

import com.aliyun.oss.OSS;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import top.kimwonjoon.domain.chat.service.armory.factory.element.AudioUtil;
import top.kimwonjoon.domain.chat.service.chat.ChatService;

import java.io.File;

/**
 * @ClassName ChatController
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/22 16:40
 */

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/ai/chat")
public class ChatController {


    @Resource
    private ChatService chatService;

    @RequestMapping(value = "chat_stream", method = {RequestMethod.POST})
    public Flux<ChatResponse> chatStream(@RequestParam("userId") String userId,@RequestParam("chatId") String chatId,@RequestParam("avatarId") String avatarId,@RequestParam("audio") MultipartFile file) {
        try{
            chatService.chat(userId, chatId, avatarId, file);
            return null;
        } catch (Exception e) {
            log.error("对话失败");
            return Flux.error(e);
        }
    }
}
