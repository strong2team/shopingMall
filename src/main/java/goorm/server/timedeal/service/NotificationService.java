package goorm.server.timedeal.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import goorm.server.timedeal.dto.SQSTimeDealDTO;
import goorm.server.timedeal.model.TimeDeal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {

	private final SimpMessagingTemplate messagingTemplate;

	public NotificationService(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	/**
	 * WebSocket 을 통해 실시간 타임딜 업데이트 메시지를 전송.
	 *
	 * @param timeDeal 타임딜 정보
	 */
	public void sendUpdateToThymeleaf(SQSTimeDealDTO timeDeal) {
		// WebSocket 경로 "/deals/updates"로 메시지 전송

		messagingTemplate.convertAndSend("/deals/updates",timeDeal);
		log.info("WebSocket 메시지를 전송했습니다: " + timeDeal);
	}
}
