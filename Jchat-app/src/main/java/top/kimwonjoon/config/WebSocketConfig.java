package top.kimwonjoon.config;

/**
 * @ClassName WebSocketConfig
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/23 09:15
 */


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 设置最大消息大小为10MB (可以根据需要调整)
        registration.setMessageSizeLimit(10 * 1024 * 1024);
        // 设置最大WebSocket帧大小
        registration.setSendBufferSizeLimit(10 * 1024 * 1024);
        // 设置时间限制
        registration.setSendTimeLimit(20 * 10000);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于向客户端发送消息
        config.enableSimpleBroker("/topic", "/queue");
        // 设置应用程序目的地前缀
        config.setApplicationDestinationPrefixes("/app");
        // 设置用户目的地前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，启用SockJS支持
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
