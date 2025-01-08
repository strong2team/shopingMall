package goorm.server.timedeal.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import goorm.server.timedeal.dto.StatusUpdateDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {

	private final SimpMessagingTemplate messagingTemplate;

	public NotificationService(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}


	/**
	 * WebSocket 을 통해 실시간 타임딜 상태 업데이트 메시지를 전송.
	 *
	 * @param timeDealId 타임딜 ID
	 * @param status 업데이트할 상태
	 */
	public void sendStatusUpdateToThymeleaf(Long timeDealId, String status) {
		StatusUpdateDto statusUpdate = new StatusUpdateDto(timeDealId, status);

		// WebSocket 경로 "/deals/status-updates"로 메시지 전송
		messagingTemplate.convertAndSend("/deals/status-updates", statusUpdate);
		log.info("WebSocket 상태 업데이트 메시지를 전송했습니다: {}", statusUpdate);
	}
}
