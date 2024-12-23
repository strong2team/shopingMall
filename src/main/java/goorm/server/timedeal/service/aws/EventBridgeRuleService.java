package goorm.server.timedeal.service.aws;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.*;

@Service
@RequiredArgsConstructor
public class EventBridgeRuleService {

	private final EventBridgeClient eventBridgeClient = EventBridgeClient.create();

	/**
	 * EventBridge Rule 생성
	 *
	 * @param ruleName Rule 이름 (고유)
	 * @param scheduleExpression CRON 표현식
	 * @param inputPayload Lambda로 전달할 데이터 (JSON 문자열)
	 * @param lambdaArn 연결할 Lambda의 ARN
	 */
	public void createEventBridgeRule(String ruleName, String scheduleExpression, String inputPayload, String lambdaArn) {
		// Rule 생성
		PutRuleRequest ruleRequest = PutRuleRequest.builder()
			.name(ruleName)
			.scheduleExpression(scheduleExpression)
			.state(RuleState.ENABLED)
			.build();
		eventBridgeClient.putRule(ruleRequest);

		// Lambda Target 연결
		Target target = Target.builder()
			.id("1")
			.arn(lambdaArn) // Lambda ARN
			.input(inputPayload) // Lambda에 전달할 JSON 데이터
			.build();

		PutTargetsRequest targetsRequest = PutTargetsRequest.builder()
			.rule(ruleName)
			.targets(target)
			.build();
		eventBridgeClient.putTargets(targetsRequest);
	}

	/**
	 * CRON 표현식 생성
	 *
	 * @param dateTime 실행 시간
	 * @return CRON 표현식
	 */
	public String convertToCronExpression(LocalDateTime dateTime) {
		return String.format("cron(%d %d %d %d ? %d)",
			dateTime.getMinute(),
			dateTime.getHour(),
			dateTime.getDayOfMonth(),
			dateTime.getMonthValue(),
			dateTime.getYear());
	}
}
