package goorm.server.timedeal.config.aws.redis;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import goorm.server.timedeal.dto.TimeDealMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TimeDealMessageListener implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(TimeDealMessageListener.class);
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			log.info("Listener is triggered");
			String body = new String(message.getBody(), StandardCharsets.UTF_8);
			TimeDealMessage timeDealMessage = objectMapper.readValue(body, TimeDealMessage.class);
			log.info("Received message: {}", timeDealMessage);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error processing message", e);
		}
	}
}
