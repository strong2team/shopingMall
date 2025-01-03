package goorm.server.timedeal.config.aws;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
public class RedisSubscriber implements MessageListener {

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String channel = new String(pattern);
		String messageBody = message.toString();

		System.out.println("Received message: " + messageBody + " from channel: " + channel);

		// 메시지를 처리 (예: DB 상태 변경 및 WebSocket 전송)
		handleTimeDealMessage(messageBody);
	}

	private void handleTimeDealMessage(String messageBody) {
		// 메시지 파싱 및 비즈니스 로직 수행
		// 예: JSON 파싱 후 DB 업데이트
		System.out.println("Processing message: " + messageBody);
	}
}
