package goorm.server.timedeal.config.aws.redis;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import goorm.server.timedeal.dto.TimeDealMessage;
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
	private RedissonClient redissonClient; // 분산 락을 위한 Redisson 클라이언트

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String messageBody = new String(message.getBody());
		String channel = new String(pattern);

		System.out.println("Received message: " + messageBody + " from channel: " + channel);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			TimeDealMessage timeDealMessage = objectMapper.readValue(messageBody, TimeDealMessage.class);

			// DB 작업은 분산 락을 이용하여 한 서버에서만 처리
			String lockKey = "time-deal-lock";
			RLock lock = redissonClient.getLock(lockKey);

			if (lock.tryLock(5, 10, TimeUnit.SECONDS)) { // 락 획득
				try {
					TimeDealStatus newStatus = TimeDealStatus.valueOf(timeDealMessage.getNew_status().toUpperCase());
					timeDealService.updateTimeDealStatus(timeDealMessage.getTime_deal_id(), newStatus);
				} finally {
					if (lock.isHeldByCurrentThread()) {
						lock.unlock(); // 락 해제
					}
				}
			} else {
				System.out.println("Another server is handling DB updates.");
			}

			// 웹소켓 전송은 모든 서버에서 실행
			notificationService.sendStatusUpdateToThymeleaf(
				timeDealMessage.getTime_deal_id(),
				timeDealMessage.getNew_status()
			);

		} catch (IllegalArgumentException e) {
			System.err.println("Invalid status received: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Error processing message: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
