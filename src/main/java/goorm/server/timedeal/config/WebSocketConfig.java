package goorm.server.timedeal.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/deals/updates");
		config.setApplicationDestinationPrefixes("/app");
	}

	// deal-updates; 클라이언트가 WebSocket 연결을 설정하기 위한 URL.
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/deal-updates").
			setAllowedOriginPatterns("*") // 모든 도메인 허용
			.withSockJS();
	}
}
