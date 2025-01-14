package goorm.server.timedeal.config.aws.sqs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import goorm.server.timedeal.dto.SQSTimeDealDTO;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Slf4j
@Component
public class SqsMessageSender {

	private final SqsTemplate queueMessagingTemplate;
	private final ObjectMapper objectMapper;


	@Value("${cloud.aws.sqs.queue-name}")
	private String queueName;

	public SqsMessageSender(SqsAsyncClient sqsAsyncClient, ObjectMapper objectMapper) {
		this.queueMessagingTemplate = SqsTemplate.newTemplate(sqsAsyncClient);
		this.objectMapper = objectMapper;
	}

	public SendResult<String> sendMessage(String messageBody) {

		// SQS 전송할 메시지
		Message<String> message = MessageBuilder.withPayload(messageBody).build();

		// 메시지를 큐에 전송하고, 전송 결과를 반환
		return queueMessagingTemplate.send(queueName, message);
	}

	public void sendJsonMessage(SQSTimeDealDTO payload) {
		try {
			// JSON 형식으로 변환
			String jsonPayload = objectMapper.writeValueAsString(payload);

			// 메시지 생성
			Message<String> message = MessageBuilder
				.withPayload(jsonPayload)
				.setHeader(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();

			// 메시지 전송
			queueMessagingTemplate.send(queueName, message);
			log.info("메시지가 성공적으로 전송되었습니다: " + jsonPayload);
		} catch (Exception e) {
			System.err.println("메시지 전송 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
