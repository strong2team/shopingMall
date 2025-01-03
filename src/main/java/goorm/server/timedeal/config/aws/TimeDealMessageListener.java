package goorm.server.timedeal.config.aws;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TimeDealMessageListener implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(TimeDealMessageListener.class);
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			System.out.println("Listener is triggered");
			logger.info("Listener is triggered");
			String body = new String(message.getBody(), StandardCharsets.UTF_8);
			TimeDealMessage timeDealMessage = objectMapper.readValue(body, TimeDealMessage.class);
			System.out.println("Received message: " + timeDealMessage);
			logger.info("Received message: {}", timeDealMessage);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error processing message", e);
		}
	}
}
