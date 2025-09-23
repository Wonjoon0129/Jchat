package top.kimwonjoon.api.dto;

/**
 * @ClassName VoiceMessage
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/23 09:17
 */

import com.fasterxml.jackson.annotation.JsonProperty;

public class VoiceMessage {

    public enum MessageType {
        VOICE_DATA,
        TEXT_MESSAGE,
        TRANSCRIPTION,
        AI_RESPONSE,
        USER_JOIN,
        USER_LEAVE
    }

    private MessageType type;
    private String sender;
    private String recipient;
    private String content;
    private String audioData; // Base64编码的音频数据
    private String audioFormat;
    private long timestamp;
    private String roomId;

    // 构造函数
    public VoiceMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public VoiceMessage(MessageType type, String sender, String content) {
        this();
        this.type = type;
        this.sender = sender;
        this.content = content;
    }

    // Getters and Setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAudioData() { return audioData; }
    public void setAudioData(String audioData) { this.audioData = audioData; }

    public String getAudioFormat() { return audioFormat; }
    public void setAudioFormat(String audioFormat) { this.audioFormat = audioFormat; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}