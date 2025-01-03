package goorm.server.timedeal.config.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import goorm.server.timedeal.model.enums.TimeDealStatus;
import goorm.server.timedeal.service.NotificationService;
import goorm.server.timedeal.service.TimeDealService;

@Service
public class RedisSubscriber implements MessageListener {

	@Autowired
	private TimeDealService timeDealService; // DB 업데이트 서비스


	@Autowired
	private NotificationService notificationService; // WebSocket 전송 서비스


	@Autowired
	private SimpMessagingTemplate messagingTemplate; // WebSocket 전송

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			String channel = new String(pattern);
			String messageBody = new String(message.getBody());
			System.out.println("Received message: " + messageBody + " from channel: " + channel);

			// JSON 파싱
			ObjectMapper objectMapper = new ObjectMapper();
			TimeDealMessage timeDealMessage = objectMapper.readValue(messageBody, TimeDealMessage.class);

			// 문자열을 Enum으로 변환
			TimeDealStatus newStatus = TimeDealStatus.valueOf(timeDealMessage.getNew_status().toUpperCase());


			// DB 업데이트
			timeDealService.updateTimeDealStatus(timeDealMessage.getTime_deal_id(), newStatus);

			// WebSocket 전송
			//messagingTemplate.convertAndSend("/deals/updates", timeDealMessage);

			notificationService.sendStatusUpdateToThymeleaf(timeDealMessage.getTime_deal_id(), newStatus.name());


		}catch (IllegalArgumentException e) {
			System.err.println("Invalid status received: " + e.getMessage());
		}  catch (Exception e) {
			System.err.println("Error processing message: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
