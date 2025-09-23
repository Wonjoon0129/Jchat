package top.kimwonjoon.api.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceMessage {

    public enum MessageType {
        VOICE_DATA,        // 语音数据
        TEXT_MESSAGE,      // 文本消息
        TRANSCRIPTION,     // 转录文本
        AI_RESPONSE_CHUNK, // AI响应块（流式）
        AI_RESPONSE_COMPLETE, // AI响应完成
        AI_RESPONSE,       // AI语音响应
        USER_JOIN,         // 用户加入
        ERROR              // 错误消息
    }

    private MessageType type;
    private String sender;
    private String content;
    private String audioData;    // Base64编码的音频数据
    private String audioFormat;  // 音频格式 (e.g., "wav", "mp3")
    private String roomId;       // 房间ID

    public VoiceMessage(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
    }
}